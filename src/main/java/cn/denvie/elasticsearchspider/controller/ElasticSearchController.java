package cn.denvie.elasticsearchspider.controller;

import cn.denvie.elasticsearchspider.es.model.*;
import cn.denvie.elasticsearchspider.es.service.ElasticsearchService;
import cn.denvie.elasticsearchspider.es.utils.SearchParamBuilder;
import cn.denvie.elasticsearchspider.spider.domain.JdGoods;
import cn.denvie.elasticsearchspider.spider.service.impl.JdGoodsParseService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author denvie
 * @date 2020/8/4
 */
@RestController
public class ElasticSearchController {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ElasticsearchService elasticSearchService;
    @Autowired
    private JdGoodsParseService jdGoodsParseService;

    /**
     * 创建京东商品索引。
     *
     * @param index 索引名称
     * @return true: 创建成功；false: 创建失败
     * @throws Exception
     */
    @PostMapping("/createJdGoodsIndex")
    public boolean createJdGoodsIndex(String index) throws IOException {
        if (elasticSearchService.isIndexExists(index)) {
            return true;
        }

        Map<String, String> settings = new HashMap<>();
        settings.put("index.number_of_shards", "1");
        settings.put("index.number_of_replicas", "1");
        Map<String, Map<String, Object>> mappings = JdGoods.mappings();

        return elasticSearchService.createIndex(index, settings, mappings);
    }

    /**
     * 爬取京东商品。
     *
     * @param keyword 京东商品关键字
     * @param pageNo  爬取第几页数据
     * @return 商品列表
     * @throws Exception
     */
    @GetMapping("/crawlJdGoods")
    public List<JdGoods> crawlJdGoods(String keyword, int pageNo) throws IOException {
        // 爬取数据
        String url = String.format("https://search.jd.com/Search?keyword=%s&page=%d", keyword, pageNo);
        List<JdGoods> jdGoodsList = jdGoodsParseService.parse(url);
        // 存入ElasticSearch
        elasticSearchService.save("jd-goods", jdGoodsList.toArray(new JdGoods[]{}));
        return jdGoodsList;
    }

    // http://localhost:8080/searchJdGoods?field=title, shop&keyword=广东人民出版社&pageNo=1&pageSize=5
    @GetMapping("/searchJdGoods")
    public PagingResult<JdGoods> searchJdGoods(String field, String keyword, int pageNo, int pageSize)
            throws IOException {
        QueryType queryType = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(field).size() > 1
                ? QueryType.MULTI_MATCH : QueryType.MATCH;
        SingleSearchParam searchParam = new SearchParamBuilder()
                .searchField(new SearchField(field, keyword, queryType, null))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .buildSingleSearchParam();
        return elasticSearchService.search("jd-goods", searchParam, JdGoods.class);
    }

    // http://localhost:8080/boolSearchJdGoods?title=连衣裙&shop=服饰专营店&startTime=2020-08-07 12:12:12&endTime=2020-08-08 12:12:12&pageNo=1&pageSize=5
    @GetMapping("/boolSearchJdGoods")
    public PagingResult<JdGoods> boolSearchJdGoods(@RequestParam Map<String, String> params)
            throws IOException {
        SearchParamBuilder searchParamBuilder = new SearchParamBuilder()
                .pageNo(NumberUtils.toInt(params.get("pageNo"), 1))
                .pageSize(NumberUtils.toInt(params.get("pageSize"), 10));
        if (StringUtils.isNoneBlank(params.get("title"))) {
            searchParamBuilder.searchField("title", params.get("title"),
                    QueryType.MATCH, QueryType.BOOL_MUST);
        }
        if (StringUtils.isNoneBlank(params.get("shop"))) {
            searchParamBuilder.searchField("shop", params.get("shop"),
                    QueryType.MATCH, QueryType.BOOL_MUST);
        }
        if (StringUtils.isNoneBlank(params.get("startTime"))) {
            try {
                Date startTime = sdf.parse(params.get("startTime"));
                RangeValue value = new RangeValue();
                value.gte(startTime);
                searchParamBuilder.searchField("createTime", value,
                        QueryType.RANGE, QueryType.BOOL_MUST);
            } catch (ParseException e) {
                // ignore
            }
        }
        if (StringUtils.isNoneBlank(params.get("endTime"))) {
            try {
                Date endTime = sdf.parse(params.get("endTime"));
                RangeValue value = new RangeValue();
                value.lte(endTime);
                searchParamBuilder.searchField("createTime", value,
                        QueryType.RANGE, QueryType.BOOL_MUST);
            } catch (ParseException e) {
                // ignore
            }
        }
        MultiSearchParam searchParam = searchParamBuilder.buildMultiSearchParam();
        return elasticSearchService.boolSearch("jd-goods", searchParam, JdGoods.class);
    }

}
