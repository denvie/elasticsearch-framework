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

import java.io.IOException;
import java.util.ArrayList;
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
    public boolean save(String index, ElasticSearchBean... sources) throws Exception {
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
    public <T> List<T> search(String index, String field, String keyword,
                                            int pageNo, int pageSize, Class<T> beanClass) throws Exception {
        SearchRequest request = new SearchRequest(index);
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, keyword);
        builder.query(matchQueryBuilder)
                .timeout(TimeValue.timeValueSeconds(TIMEOUT_SECONDS))
                .from((pageNo - 1) * pageSize)
                .size(pageSize).highlighter();
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(field);
        highlightBuilder.preTags("<span style='color=red;'>");
        highlightBuilder.postTags("</span>");
        builder.highlighter(highlightBuilder);
        request.source(builder);

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        List<T> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            // 解析高亮的字段
            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
            HighlightField highlightField = highlightFieldMap.get(field);
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if (highlightField != null) {
                Text[] texts = highlightField.fragments();
                StringBuilder sb = new StringBuilder();
                for (Text text : texts) {
                    sb.append(text.string());
                }
                // 高亮的字段替换掉原来的内容
                sourceAsMap.put(field, sb.toString());
            }
            result.add(objectMapper.readValue(objectMapper.writeValueAsString(sourceAsMap), beanClass));
        }
        return result;
    }

}
