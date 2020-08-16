/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearchspider.es.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch 映射工具类。
 *
 * @author denvie
 * @since 2020/8/7
 */
public class MappingUtils {
    /**
     * 关键字映射
     */
    public static Map<String, Object> keywordMapping() {
        Map<String, Object> keywordFieldMapping = new HashMap<>();
        keywordFieldMapping.put("type", "keyword");
        return keywordFieldMapping;
    }

    /**
     * 文本映射
     */
    public static Map<String, Object> textMapping() {
        Map<String, Object> textFieldMapping = new HashMap<>();
        textFieldMapping.put("type", "text");
        return textFieldMapping;
    }

    /**
     * IK分词器映射
     */
    public static Map<String, Object> ikMapping() {
        Map<String, Object> ikFieldMapping = new HashMap<>();
        ikFieldMapping.put("type", "text");
        ikFieldMapping.put("analyzer", "ik_max_word");
        ikFieldMapping.put("search_analyzer", "ik_smart");
        return ikFieldMapping;
    }

    /**
     * 日期映射
     */
    public static Map<String, Object> dateMapping() {
        Map<String, Object> dateFieldMapping = new HashMap<>();
        dateFieldMapping.put("type", "date");
        return dateFieldMapping;
    }
}
