package cn.denvie.elasticsearchspider.spider.domain;

import cn.denvie.elasticsearchspider.es.model.EsIndexBean;
import cn.denvie.elasticsearchspider.es.utils.MappingUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private Date createTime;

    @Override
    public String getEsIndexId() {
        if (StringUtils.isBlank(link)) {
            if (StringUtils.isBlank(esIndexId)) {
                esIndexId = UUID.randomUUID().toString().replace("-", "");
            }
            return esIndexId;
        }
        return link.hashCode() + "";
    }

    public static Map<String, Map<String, Object>> mappings() {
        Map<String, Map<String, Object>> mappings = new HashMap<>();
        mappings.put("link", MappingUtils.textMapping());
        mappings.put("title", MappingUtils.ikMapping());
        mappings.put("img", MappingUtils.textMapping());
        mappings.put("price", MappingUtils.textMapping());
        mappings.put("shop", MappingUtils.ikMapping());
        mappings.put("createTime", MappingUtils.dateMapping());
        return mappings;
    }
}
