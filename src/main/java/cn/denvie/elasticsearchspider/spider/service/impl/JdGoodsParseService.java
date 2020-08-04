package cn.denvie.elasticsearchspider.spider.service.impl;

import cn.denvie.elasticsearchspider.spider.domain.JdGoods;
import cn.denvie.elasticsearchspider.spider.service.HtmlParseService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 京东商品解析服务。
 *
 * @author denvie
 * @date 2020/8/4
 */
@Service
public class JdGoodsParseService implements HtmlParseService<List<JdGoods>> {

    @Override
    public List<JdGoods> parse(String url) throws IOException {
        List<JdGoods> result = new ArrayList<>();
        Document document = Jsoup.parse(new URL(url), 30000);
        // 获取商品列表元素
        Element goodsRootElement = document.getElementById("J_goodsList");
        Elements goodsElements = goodsRootElement.getElementsByTag("li");
        if (goodsElements == null) {
            return result;
        }
        for (Element element : goodsElements) {
            String link = element.getElementsByTag("a").eq(0).attr("href");
            String title = element.getElementsByClass("p-name").eq(0).text();
            String img = element.getElementsByTag("img").eq(0).attr("src");
            if (StringUtils.isEmpty(img)) {
                // 如果图片是延迟加载的，需要通过 source-data-lazy-img 获取
                img = element.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            }
            String price = element.getElementsByClass("p-price").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            result.add(new JdGoods(null, link, title, img, price, shop));
        }
        return result;
    }

}
