/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.spider.domain;

import cn.denvie.elasticsearch.model.EsIndexBean;
import cn.denvie.elasticsearch.utils.MappingUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jd Goods Bean.
 *
 * @author denvie
 * @since 2020/8/4
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

    /**
     * Jd Goods 索引映射。
     *
     * @return Map<String, Map < String, Object>>
     */
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
