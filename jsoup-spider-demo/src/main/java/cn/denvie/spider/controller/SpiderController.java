/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.spider.controller;

import cn.denvie.elasticsearch.model.*;
import cn.denvie.elasticsearch.service.ElasticsearchService;
import cn.denvie.elasticsearch.utils.AggregationUtils;
import cn.denvie.elasticsearch.utils.SearchParamBuilder;
import cn.denvie.elasticsearch.utils.SettingBuilder;
import cn.denvie.spider.domain.JdGoods;
import cn.denvie.spider.service.impl.JdGoodsParseService;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Spider controller.
 *
 * @author denvie
 * @since 2020/8/4
 */
@RestController
public class SpiderController {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String index = "jd-goods";

    @Autowired
    private ElasticsearchService elasticSearchService;
    @Autowired
    private JdGoodsParseService jdGoodsParseService;

    /**
     * 创建索引。
     */
    @PostMapping("/createJdGoodsIndex")
    public boolean createJdGoodsIndex(String index) throws IOException {
        if (elasticSearchService.isIndexExists(index)) {
            return true;
        }

        Map<String, String> settings = new SettingBuilder()
                .number_of_replicas("1")
                .numberOfShards("1")
                .build();
        Map<String, Map<String, Object>> mappings = JdGoods.mappings();

        return elasticSearchService.createIndex(index, settings, mappings);
    }

    /**
     * 爬取京东商品。
     */
    @GetMapping("/crawlJdGoods")
    public List<JdGoods> crawlJdGoods(String keyword, int pageNo) throws IOException {
        // 爬取数据
        String url = String.format("https://search.jd.com/Search?keyword=%s&page=%d", keyword, pageNo);
        List<JdGoods> jdGoodsList = jdGoodsParseService.parse(url);
        // 存入ElasticSearch
        elasticSearchService.saveDocument(index, jdGoodsList.toArray(new JdGoods[]{}));
        return jdGoodsList;
    }

    @GetMapping("/searchJdGoods")
    public SearchResult<JdGoods> searchJdGoods(String field, String keyword, int pageNo, int pageSize)
            throws IOException {
        List<String> fieldList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(field);
        SearchType searchType = fieldList.size() > 1 ? SearchType.MULTI_MATCH : SearchType.MATCH;
        SingleSearchParam searchParam = new SearchParamBuilder()
                .searchField(new SearchField(field, keyword, searchType, null, false))
                .highlightField(new HighlightField(
                        fieldList.get(0), "<span style='color: read;'>", "</span>"))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .buildSingleSearchParam();
        return elasticSearchService.searchDocuments(index, searchParam, JdGoods.class);
    }

    @GetMapping("/boolSearchJdGoods")
    public SearchResult<JdGoods> boolSearchJdGoods(@RequestParam Map<String, String> params)
            throws IOException {
        SearchParamBuilder searchParamBuilder = new SearchParamBuilder()
                .trackTotalHits(true)
                .pageNo(NumberUtils.toInt(params.get("pageNo"), 1))
                .pageSize(NumberUtils.toInt(params.get("pageSize"), 10));
        if (StringUtils.isNoneBlank(params.get("title"))) {
            searchParamBuilder.searchField("title", params.get("title"),
                    SearchType.MATCH, SearchType.BOOL_MUST, false);
        }
        if (StringUtils.isNoneBlank(params.get("shop"))) {
            searchParamBuilder.searchField("shop", params.get("shop"),
                    SearchType.MATCH, SearchType.BOOL_MUST, false);
        }
        if (StringUtils.isNoneBlank(params.get("startTime"))) {
            try {
                Date startTime = sdf.parse(params.get("startTime"));
                RangeValue value = new RangeValue();
                value.gte(startTime);
                searchParamBuilder.searchField("createTime", value,
                        SearchType.RANGE, SearchType.BOOL_MUST, false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isNoneBlank(params.get("endTime"))) {
            try {
                Date endTime = sdf.parse(params.get("endTime"));
                RangeValue value = new RangeValue();
                value.lte(endTime);
                searchParamBuilder.searchField("createTime", value,
                        SearchType.RANGE, SearchType.BOOL_MUST, false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        MultiSearchParam searchParam = searchParamBuilder.buildMultiSearchParam();
        return elasticSearchService.boolSearchDocuments(index, searchParam, JdGoods.class);
    }

    @GetMapping("/aggregationSearchJdGoods")
    public String aggregationSearch(String field, String keyword) throws IOException {
        SearchParamBuilder searchParamBuilder = new SearchParamBuilder()
                .searchField(new SearchField(field, keyword, SearchType.MATCH, null, true))
                .aggregation(AggregationUtils.dateHistogram("group_by_date", "createTime"))
                .withOriginalResponse(true).pageNo(-1).pageSize(0);
        return elasticSearchService.searchDocuments(index,
                searchParamBuilder.buildSingleSearchParam(), null).toString();
    }
}
