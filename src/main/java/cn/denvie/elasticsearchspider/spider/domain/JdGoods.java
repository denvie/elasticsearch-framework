package cn.denvie.elasticsearchspider.spider.domain;

import cn.denvie.elasticsearchspider.es.model.EsIndexBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author denvie
 * @date 2020/8/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdGoods implements EsIndexBean {

    private String esIndexId;
    private String link;
    private String title;
    private String img;
    private String price;
    private String shop;

    @Override
    public String getEsIndexId() {
        if (StringUtils.isEmpty(link)) {
            if (StringUtils.isEmpty(esIndexId)) {
                esIndexId = UUID.randomUUID().toString().replace("-", "");
            }
            return esIndexId;
        }
        return link.hashCode() + "";
    }
}
