/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.utils;

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
     * 关键字映射。
     *
     * @return 字段属性映射设置
     */
    public static Map<String, Object> keywordMapping() {
        Map<String, Object> keywordFieldMapping = new HashMap<>();
        keywordFieldMapping.put("type", "keyword");
        return keywordFieldMapping;
    }

    /**
     * 文本映射。
     *
     * @return Map<String, Object>字段属性映射设置
     */
    public static Map<String, Object> textMapping() {
        Map<String, Object> textFieldMapping = new HashMap<>();
        textFieldMapping.put("type", "text");
        return textFieldMapping;
    }

    /**
     * 日期映射。
     *
     * @return Map<String, Object>字段属性映射设置
     */
    public static Map<String, Object> dateMapping() {
        Map<String, Object> dateFieldMapping = new HashMap<>();
        dateFieldMapping.put("type", "date");
        return dateFieldMapping;
    }

    /**
     * simple分词器映射，不支持中文分词。
     *
     * @return Map<String, Object>字段属性映射设置
     */
    public static Map<String, Object> simpleMapping() {
        Map<String, Object> simpleFieldMapping = new HashMap<>();
        simpleFieldMapping.put("type", "text");
        simpleFieldMapping.put("analyzer", "simple");
        return simpleFieldMapping;
    }

    /**
     * stop分词器映射，不支持中文分词。
     *
     * @return Map<String, Object>字段属性映射设置
     */
    public static Map<String, Object> stopMapping() {
        Map<String, Object> stopFieldMapping = new HashMap<>();
        stopFieldMapping.put("type", "text");
        stopFieldMapping.put("analyzer", "stop");
        return stopFieldMapping;
    }

    /**
     * IK分词器映射，中文分词最佳解决方案。
     *
     * @return Map<String, Object>字段属性映射设置
     */
    public static Map<String, Object> ikMapping() {
        Map<String, Object> ikFieldMapping = new HashMap<>();
        ikFieldMapping.put("type", "text");
        ikFieldMapping.put("analyzer", "ik_max_word");
        ikFieldMapping.put("search_analyzer", "ik_smart");
        return ikFieldMapping;
    }
}
