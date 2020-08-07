package cn.denvie.elasticsearchspider.controller;

import cn.denvie.elasticsearchspider.es.model.PagingResult;
import cn.denvie.elasticsearchspider.es.model.QueryType;
import cn.denvie.elasticsearchspider.es.model.SearchField;
import cn.denvie.elasticsearchspider.es.model.SearchParam;
import cn.denvie.elasticsearchspider.es.service.ElasticsearchService;
import cn.denvie.elasticsearchspider.spider.domain.JdGoods;
import cn.denvie.elasticsearchspider.spider.service.impl.JdGoodsParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author denvie
 * @date 2020/8/4
 */
@RestController
public class ElasticSearchController {

    @Autowired
    private ElasticsearchService elasticSearchService;

    @Autowired
    private JdGoodsParseService jdGoodsParseService;

    /**
     * 创建索引。
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

    @GetMapping("/searchJdGoods")
    public PagingResult<JdGoods> searchJdGoods(String keyword, int pageNo, int pageSize) throws IOException {
        SearchParam searchParam = new SearchParam.Builder()
                .searchField(new SearchField("title", keyword, QueryType.MATCH))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        return elasticSearchService.search("jd-goods", searchParam, JdGoods.class);
    }

}
