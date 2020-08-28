/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.service.impl;

import cn.denvie.elasticsearch.model.*;
import cn.denvie.elasticsearch.service.ElasticsearchService;
import cn.denvie.elasticsearch.utils.BeanMapUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch服务实现类。
 *
 * @author denvie
 * @since 2020/8/4
 */
public class ElasticsearchServiceImpl implements ElasticsearchService {
    private final RestHighLevelClient restHighLevelClient;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;

    public ElasticsearchServiceImpl(RestHighLevelClient restHighLevelClient, int timeoutSeconds) {
        this.restHighLevelClient = restHighLevelClient;
        this.timeoutSeconds = timeoutSeconds <= 0 ? 30 : timeoutSeconds;
        this.objectMapper = new ObjectMapper();
    }

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
    public boolean deleteIndex(String index) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public boolean saveDocument(String index, EsIndexBean... sources) throws IOException {
        if (sources == null || sources.length == 0) {
            return false;
        }
        // 批处理请求
        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(timeoutSeconds));
        for (EsIndexBean source : sources) {
            request.add(new IndexRequest(index)
                    .id(source.getEsIndexId())
                    .source(BeanMapUtils.beanToMap(source)));
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return !response.hasFailures();
    }

    @Override
    public <T> T getDocument(String index, String id, Class<T> docClass) throws IOException {
        GetRequest request = new GetRequest(index, id);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        return BeanMapUtils.mapToBean(response.getSourceAsMap(), docClass);
    }

    @Override
    public boolean updateDocument(String index, String id, EsIndexBean source) throws IOException {
        UpdateRequest request = new UpdateRequest(index, id);
        request.doc(objectMapper.writeValueAsString(source), XContentType.JSON);
        UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        return response.status() == RestStatus.OK;
    }

    @Override
    public boolean deleteDocument(String index, String id) throws IOException {
        DeleteRequest request = new DeleteRequest(index, id);
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        return response.status() == RestStatus.OK;
    }

    @Override
    public <T> SearchResult<T> searchAllDocuments(String indexes, AbstractSearchParam searchParam, Class<T> docClass)
            throws IOException {
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());
        // 设置搜索参数
        if (searchParam != null) {
            setupSearchBuilder(builder, searchParam);
        }
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, docClass);
    }

    @Override
    public <T> SearchResult<T> searchDocuments(String indexes, SingleSearchParam searchParam, Class<T> docClass)
            throws IOException {
        if (searchParam == null || searchParam.getSearchField() == null) {
            return searchAllDocuments(indexes, searchParam, docClass);
        }
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        SearchField searchField = searchParam.getSearchField();
        // 匹配搜索类型，如：match, multi_match, term, range
        builder.query(getSearchType(searchField));
        // 设置搜索参数
        setupSearchBuilder(builder, searchParam);
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, docClass);
    }

    @Override
    public <T> SearchResult<T> boolSearchDocuments(String indexes, MultiSearchParam searchParam,
                                                   Class<T> docClass) throws IOException {
        if (searchParam == null || searchParam.getSearchFieldList() == null
                || searchParam.getSearchFieldList().isEmpty()) {
            return searchAllDocuments(indexes, searchParam, docClass);
        }
        SearchRequest request = new SearchRequest(indexes);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 设置搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<SearchField> searchFieldList = searchParam.getSearchFieldList();
        for (SearchField field : searchFieldList) {
            switch (field.getOuterSearchType()) {
                case BOOL_MUST:
                    boolQueryBuilder.must(getSearchType(field));
                    break;
                case BOOL_MUST_NOT:
                    boolQueryBuilder.mustNot(getSearchType(field));
                    break;
                case BOOL_MUST_SHOULD:
                    boolQueryBuilder.should(getSearchType(field));
                    break;
                case BOOL_MUST_FILTER:
                    boolQueryBuilder.filter(getSearchType(field));
                    break;
                default:
                    throw new RuntimeException("not supported query type: " + field.getSearchType().getQueryName());
            }
        }
        builder.query(boolQueryBuilder);
        // 设置搜索参数
        setupSearchBuilder(builder, searchParam);
        // 执行搜索
        request.source(builder);
        return commitSearch(request, searchParam, docClass);
    }

    private QueryBuilder getSearchType(SearchField searchField) {
        if (searchField.isConstantScore()) {
            return QueryBuilders.constantScoreQuery(caseQueryBuilder(searchField));
        }
        return caseQueryBuilder(searchField);
    }

    private QueryBuilder caseQueryBuilder(SearchField searchField) {
        switch (searchField.getSearchType()) {
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
                throw new RuntimeException("not supported search type: "
                        + searchField.getSearchType().getQueryName());
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
        if (searchParam.getPageNo() > 0) {
            builder.from((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        }
        builder.size(searchParam.getPageSize());
        // 设置聚合搜索项
        if (searchParam.getAggregationBuilders() != null && searchParam.getAggregationBuilders().size() > 0) {
            for (AggregationBuilder aggregation : searchParam.getAggregationBuilders()) {
                builder.aggregation(aggregation);
            }
        }
        // 设置是否获取实际文档总数
        builder.trackTotalHits(searchParam.isTrackTotalHits());
    }

    private <T> SearchResult<T> commitSearch(SearchRequest request, AbstractSearchParam searchParam,
                                             Class<T> docClass) throws IOException {
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<T> result = null;
        if (docClass != null) {
            result = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> sourceAsMap = resolveHighlightField(hit, searchParam.getHighlightField(),
                        searchParam.getHighlightPreTags(), searchParam.getHighlightPostTags());
                try {
                    result.add(BeanMapUtils.mapToBean(sourceAsMap, docClass));
                } catch (Exception e) {
                    result.add(objectMapper.readValue(objectMapper.writeValueAsString(sourceAsMap), docClass));
                }
            }
        }
        return new SearchResult<>(response.getHits().getTotalHits().value, result, response,
                searchParam.getPageNo(), searchParam.getPageSize());
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
}
