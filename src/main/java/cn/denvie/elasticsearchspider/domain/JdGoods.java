package cn.denvie.elasticsearchspider.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdGoods implements ElasticSearchBean {

    private String link;
    private String title;
    private String img;
    private String price;
    private String shop;

    @Override
    public String getElasticSearchIndexId() {
        return link == null ?
                UUID.randomUUID().toString().replace("-", "") : link.hashCode() + "";
    }
}
