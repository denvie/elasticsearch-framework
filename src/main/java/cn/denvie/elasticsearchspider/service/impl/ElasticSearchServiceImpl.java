package cn.denvie.elasticsearchspider.service.impl;

import cn.denvie.elasticsearchspider.domain.ElasticSearchBean;
import cn.denvie.elasticsearchspider.service.ElasticSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private static final int TIMEOUT_SECONDS = 30;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean createIndex(String index) throws Exception {
        return createIndex(index, null, null);
    }

    @Override
    public boolean createIndex(String index, Map<String, String> settings,
                               Map<String, Map<String, Object>> mappings) throws Exception {
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
    public boolean isIndexExists(String index) throws Exception {
        GetIndexRequest request = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean saveDocuments(String index, ElasticSearchBean... sources) throws Exception {
        if (sources == null || sources.length == 0) {
            return false;
        }
        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(TIMEOUT_SECONDS));
        // 批处理请求
        for (ElasticSearchBean source : sources) {
            request.add(new IndexRequest(index)
                    .id(source.getElasticSearchIndexId())
                    .source(objectMapper.writeValueAsString(source), XContentType.JSON));
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return !response.hasFailures();
    }

    @Override
    public <T> List<T> searchDocuments(String index, String field, String keyword,
                                       int pageNo, int pageSize, Class<T> beanClass) throws Exception {
        return searchDocuments(index, field, keyword, null, null, pageNo, pageSize, beanClass);
    }

    @Override
    public <T> List<T> searchDocuments(String index, String field, String keyword, String preTags, String postTags,
                                       int pageNo, int pageSize, Class<T> beanClass) throws Exception {
        SearchRequest request = new SearchRequest(index);
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, keyword);
        builder.query(matchQueryBuilder)
                .timeout(TimeValue.timeValueSeconds(TIMEOUT_SECONDS))
                .from((pageNo - 1) * pageSize)
                .size(pageSize).highlighter();
        // 高亮查询
        if (!StringUtils.isEmpty(preTags) && !StringUtils.isEmpty(postTags)) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(field);
            highlightBuilder.preTags(preTags);
            highlightBuilder.postTags(postTags);
            builder.highlighter(highlightBuilder);
            request.source(builder);
        }

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<T> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 解析高亮的字段
            if (!StringUtils.isEmpty(preTags) && !StringUtils.isEmpty(postTags)) {
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
            result.add(objectMapper.readValue(objectMapper.writeValueAsString(sourceAsMap), beanClass));
        }
        return result;
    }

}
