package cn.denvie.elasticsearchspider.controller;

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

    @PostMapping("/createJdGoodsIndex")
    public boolean createJdGoodsIndex(String index) throws Exception {
        if (elasticSearchService.isIndexExists(index)) {
            return true;
        }

        Map<String, String> settings = new HashMap<>();
        settings.put("index.number_of_shards", "1");
        settings.put("index.number_of_replicas", "1");

        Map<String, Object> normalFieldMapping = new HashMap<>();
        normalFieldMapping.put("type", "text");
        Map<String, Object> ikFieldMapping = new HashMap<>();
        ikFieldMapping.put("type", "text");
        ikFieldMapping.put("analyzer", "ik_max_word");
        ikFieldMapping.put("search_analyzer", "ik_smart");

        Map<String, Map<String, Object>> mappings = new HashMap<>();
        mappings.put("link", normalFieldMapping);
        mappings.put("title", ikFieldMapping);
        mappings.put("img", normalFieldMapping);
        mappings.put("price", normalFieldMapping);
        mappings.put("shop", ikFieldMapping);

        return elasticSearchService.createIndex(index, settings, mappings);
    }

    @GetMapping("/crawlJdGoods")
    public List<JdGoods> crawlJdGoods(String keyword, int pageNo) throws Exception {
        // 爬取数据
        String url = String.format("https://search.jd.com/Search?keyword=%s&page=%d", keyword, pageNo);
        List<JdGoods> jdGoodsList = jdGoodsParseService.parse(url);
        // 存入ElasticSearch
        elasticSearchService.save("jd-goods", jdGoodsList.toArray(new JdGoods[]{}));
        return jdGoodsList;
    }

    @GetMapping("/searchJdGoods")
    public List<JdGoods> searchJdGoods(String keyword, int pageNo, int pageSize) throws Exception {
        SearchParam searchParam = new SearchParam.Builder()
                .addSearchField(new SearchField("title", keyword, QueryType.MATCH))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        return elasticSearchService.search("jd-goods", searchParam, JdGoods.class);
    }

}
