package cn.denvie.elasticsearchspider.controller;

import cn.denvie.elasticsearchspider.domain.JdGoods;
import cn.denvie.elasticsearchspider.service.ElasticSearchService;
import cn.denvie.elasticsearchspider.service.HtmlParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ElasticSearchController {

    @Autowired
    private HtmlParseService jdGoodsParseService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/crawlJdGoods")
    public List<JdGoods> crawlJdGoods(String keyword, int pageNo) throws Exception {
        // 爬取数据
        String url = String.format("https://search.jd.com/Search?keyword=%s&page=%d", keyword, pageNo);
        List<JdGoods> jdGoodsList = (List<JdGoods>) jdGoodsParseService.parse(url);
        // 存入ElasticSearch
        elasticSearchService.save("jd-goods", jdGoodsList.toArray(new JdGoods[]{}));
        return jdGoodsList;
    }

    @GetMapping("/searchJdGoods")
    public List<JdGoods> searchJdGoods(String keyword, int pageNo, int pageSize) throws Exception {
        return elasticSearchService.search("jd-goods", "title",
                keyword, pageNo, pageSize, JdGoods.class);
    }

}
