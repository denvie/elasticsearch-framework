package cn.denvie.elasticsearchspider.es.service.impl;

import cn.denvie.elasticsearchspider.es.model.*;
import cn.denvie.elasticsearchspider.es.service.ElasticsearchService;
import cn.denvie.elasticsearchspider.es.utils.BeanMapUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
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
import org.elasticsearch.index.query.*;
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
    public <T> PagingResult<T> searchAll(String indexes, AbstractSearchParam searchParam, Class<T> beanClass)
            throws IOException {
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 设置搜索参数
        if (searchParam != null) {
            setupSearchBuilder(builder, searchParam);
        }
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, beanClass);
    }

    @Override
    public <T> PagingResult<T> search(String indexes, SingleSearchParam searchParam, Class<T> beanClass)
            throws IOException {
        if (searchParam == null || searchParam.getSearchField() == null) {
            return searchAll(indexes, searchParam, beanClass);
        }
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        SearchField searchField = searchParam.getSearchField();
        // 匹配搜索类型，如：match, multi_match, term, range
        builder.query(castSearchType(searchField));
        // 设置搜索参数
        setupSearchBuilder(builder, searchParam);
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, beanClass);
    }

    @Override
    public <T> PagingResult<T> boolSearch(String indexes, MultiSearchParam searchParam,
                                          Class<T> beanClass) throws IOException {
        if (searchParam == null || searchParam.getSearchFieldList() == null
                || searchParam.getSearchFieldList().isEmpty()) {
            return searchAll(indexes, searchParam, beanClass);
        }
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 设置搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<SearchField> searchFieldList = searchParam.getSearchFieldList();
        for (SearchField field : searchFieldList) {
            switch (field.getBoolType()) {
                case BOOL_MUST:
                    boolQueryBuilder.must(castSearchType(field));
                    break;
                case BOOL_MUST_NOT:
                    boolQueryBuilder.mustNot(castSearchType(field));
                    break;
                case BOOL_MUST_SHOULD:
                    boolQueryBuilder.should(castSearchType(field));
                    break;
                case BOOL_MUST_FILTER:
                    boolQueryBuilder.filter(castSearchType(field));
                    break;
                default:
                    throw new RuntimeException("not supported query type: "
                            + field.getQueryType().getQueryName());
            }
        }
        builder.query(boolQueryBuilder);
        // 设置搜索参数
        setupSearchBuilder(builder, searchParam);
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, beanClass);
    }

    private QueryBuilder castSearchType(SearchField searchField) {
        switch (searchField.getQueryType()) {
            case MATCH:
                return QueryBuilders.matchQuery(searchField.getName(), searchField.getValue());
            case MULTI_MATCH:
                String[] fieldNames = Splitter.on(",")
                        .trimResults()
                        .splitToList(searchField.getName())
                        .toArray(new String[]{});
                MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(
                        searchField.getValue(), fieldNames);
                multiMatchQuery.type(MultiMatchQueryBuilder.Type.BEST_FIELDS);
                return multiMatchQuery;
            case TERM:
                return QueryBuilders.termQuery(searchField.getName(), searchField.getValue());
            case RANGE:
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(searchField.getName());
                ((RangeValue) searchField.getValue()).inflate(rangeQueryBuilder);
                return rangeQueryBuilder;
            default:
                throw new RuntimeException("not supported query type: "
                        + searchField.getQueryType().getQueryName());
        }
    }

    private void setupSearchBuilder(SearchSourceBuilder builder, AbstractSearchParam searchParam) {
        // 设置高亮搜索
        if (searchParam.getHighlightField() != null
                && !StringUtils.isBlank(searchParam.getHighlightPreTags())
                && !StringUtils.isBlank(searchParam.getHighlightPostTags())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field(searchParam.getHighlightField().getName());
            highlightBuilder.preTags(searchParam.getHighlightPreTags());
            highlightBuilder.postTags(searchParam.getHighlightPostTags());
            builder.highlighter(highlightBuilder);
        }
        // 设置排序
        if (searchParam.getOrderField() != null) {
            builder.sort(searchParam.getOrderField().getName(), searchParam.getOrderField().getSortOrder());
        }
        // 设置分页参数
        builder.from((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        builder.size(searchParam.getPageSize());
    }

    private Map<String, Object> resolveHighlightField(SearchHit hit, SearchField highlightField,
                                                      String preTags, String postTags) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        // 解析高亮的字段
        if (highlightField != null && !StringUtils.isBlank(preTags) && !StringUtils.isBlank(postTags)) {
            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField highlight = highlightFieldMap.get(highlightField.getName());
            if (highlight != null) {
                Text[] texts = highlight.fragments();
                StringBuilder sb = new StringBuilder();
                for (Text text : texts) {
                    sb.append(text.string());
                }
                // 使用高亮的字段替换掉原来的内容
                sourceAsMap.put(highlightField.getName(), sb.toString());
            }
        }
        return sourceAsMap;
    }

    private <T> PagingResult<T> commitSearch(SearchRequest request, AbstractSearchParam searchParam,
                                             Class<T> beanClass) throws IOException {
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<T> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            Map<String, Object> sourceAsMap = resolveHighlightField(hit, searchParam.getHighlightField(),
                    searchParam.getHighlightPreTags(), searchParam.getHighlightPostTags());
            try {
                result.add(BeanMapUtils.mapToBean(sourceAsMap, beanClass.newInstance()));
            } catch (Exception e) {
                result.add(objectMapper.readValue(objectMapper.writeValueAsString(sourceAsMap), beanClass));
            }
        }
        return new PagingResult<>(hits.getTotalHits().value, result,
                searchParam.getPageNo(), searchParam.getPageSize());
    }
}
