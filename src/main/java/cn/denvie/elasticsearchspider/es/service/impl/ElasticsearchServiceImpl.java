package cn.denvie.elasticsearchspider.es.service.impl;

import cn.denvie.elasticsearchspider.es.model.EsIndexBean;
import cn.denvie.elasticsearchspider.es.model.RangeValue;
import cn.denvie.elasticsearchspider.es.model.SearchField;
import cn.denvie.elasticsearchspider.es.model.SearchParam;
import cn.denvie.elasticsearchspider.es.service.ElasticsearchService;
import cn.denvie.elasticsearchspider.es.utils.BeanMapUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Value("${elasticsearch.timeoutSeconds}")
    private int timeoutSeconds = 30;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean createIndex(String index) throws IOException {
        return createIndex(index, null, null);
    }

    @Override
    public boolean createIndex(String index, Map<String, String> settings,
                               Map<String, Map<String, Object>> mappings) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);

        // index settings
        if (settings != null && !settings.isEmpty()) {
            Settings.Builder settingsBuilder = Settings.builder();
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                settingsBuilder.put(entry.getKey(), entry.getValue());
            }
            request.settings(settingsBuilder);
        }
        // index mappings
        if (mappings != null && !mappings.isEmpty()) {
            Map<String, Object> properties = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> fieldMappings : mappings.entrySet()) {
                properties.put(fieldMappings.getKey(), fieldMappings.getValue());
            }
            Map<String, Object> indexMapping = new HashMap<>();
            indexMapping.put("properties", properties);
            request.mapping(indexMapping);
        }

        CreateIndexResponse response = restHighLevelClient.indices().create(
                request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public boolean isIndexExists(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean save(String index, EsIndexBean... sources) throws IOException {
        if (sources == null || sources.length == 0) {
            return false;
        }
        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(timeoutSeconds));
        // 批处理请求
        for (EsIndexBean source : sources) {
            request.add(new IndexRequest(index)
                    .id(source.getEsIndexId())
                    .source(BeanMapUtils.beanToMap(source)));
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return !response.hasFailures();
    }

    @Override
    public <T> List<T> search(String indexes, SearchParam searchParam, Class<T> beanClass)
            throws IOException {
        if (searchParam == null || searchParam.getSearchFieldList() == null
                || searchParam.getSearchFieldList().isEmpty()) {
            return null;
        }
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        SearchField searchField = searchParam.getSearchFieldList().get(0);
        // 设置分页参数
        builder.from((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        builder.size(searchParam.getPageSize());
        // 构建搜索条件，如：match, term, range
        switch (searchField.getQueryType()) {
            case MATCH:
                builder.query(QueryBuilders.matchQuery(searchField.getName(), searchField.getValue()));
                break;
            case TERM:
                builder.query(QueryBuilders.termQuery(searchField.getName(), searchField.getValue()));
                break;
            case RANGE:
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(searchField.getName());
                ((RangeValue)searchField.getValue()).inflate(rangeQueryBuilder);
                builder.query(rangeQueryBuilder);
                break;
            default:
                throw new RuntimeException("not supported query type: "
                        + searchField.getQueryType().getQueryName());
        }
        // 设置高亮搜索
        if (!StringUtils.isBlank(searchParam.getHighlightPreTags())
                && !StringUtils.isBlank(searchParam.getHighlightPostTags())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(searchField.getName());
            highlightBuilder.preTags(searchParam.getHighlightPreTags());
            highlightBuilder.postTags(searchParam.getHighlightPostTags());
            builder.highlighter(highlightBuilder);
            request.source(builder);
        }
        // 设置排序
        if (searchParam.getOrderField() != null) {
            builder.sort(searchParam.getOrderField().getName(), searchParam.getOrderField().getSortOrder());
        }
        // 执行搜索
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<T> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = resolveHighlightField(hit, searchField.getName(),
                    searchParam.getHighlightPreTags(), searchParam.getHighlightPostTags());
            try {
                result.add(BeanMapUtils.mapToBean(sourceAsMap, beanClass.newInstance()));
            } catch (Exception e) {
                result.add(objectMapper.readValue(objectMapper.writeValueAsString(sourceAsMap), beanClass));
            }
        }
        return result;
    }

    private Map<String, Object> resolveHighlightField(SearchHit hit, String field,
                                                      String preTags, String postTags) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        // 解析高亮的字段
        if (!StringUtils.isBlank(preTags) && !StringUtils.isBlank(postTags)) {
            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField highlightField = highlightFieldMap.get(field);
            if (highlightField != null) {
                Text[] texts = highlightField.fragments();
                StringBuilder sb = new StringBuilder();
                for (Text text : texts) {
                    sb.append(text.string());
                }
                // 使用高亮的字段替换掉原来的内容
                sourceAsMap.put(field, sb.toString());
            }
        }
        return sourceAsMap;
    }

}
