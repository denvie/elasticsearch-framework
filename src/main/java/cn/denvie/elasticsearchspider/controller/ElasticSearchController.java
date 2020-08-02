package cn.denvie.elasticsearchspider.controller;

import cn.denvie.elasticsearchspider.domain.JdGoods;
import cn.denvie.elasticsearchspider.service.ElasticSearchService;
import cn.denvie.elasticsearchspider.service.HtmlParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ElasticSearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private HtmlParseService jdGoodsParseService;

    @PostMapping("/createJdGoodsIndex")
    public boolean createJdGoodsIndex(@NotNull String index) throws Exception {
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
        ikFieldMapping.put("analyzer", "ik_smart");
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
        List<JdGoods> jdGoodsList = (List<JdGoods>) jdGoodsParseService.parse(url);
        // 存入ElasticSearch
        elasticSearchService.saveDocuments("jd-goods", jdGoodsList.toArray(new JdGoods[]{}));
        return jdGoodsList;
    }

    @GetMapping("/searchJdGoods")
    public List<JdGoods> searchJdGoods(String keyword, int pageNo, int pageSize) throws Exception {
        return elasticSearchService.searchDocuments("jd-goods", "title",
                keyword, pageNo, pageSize, JdGoods.class);
    }

}
