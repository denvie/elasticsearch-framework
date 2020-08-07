package cn.denvie.elasticsearchspider.es.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch 字段映射工具类。
 *
 * @author denvie
 * @date 2020/8/7
 */
public class MappingUtils {
    /**
     * 关键字映射
     */
    private static Map<String, Object> keywordFieldMapping;
    /**
     * 文本映射
     */
    private static Map<String, Object> textFieldMapping;
    /**
     * IK分词器映射
     */
    private static Map<String, Object> ikFieldMapping;
    /**
     * 日期映射
     */
    private static Map<String, Object> dateFieldMapping;

    static {
        keywordFieldMapping = new HashMap<>();
        keywordFieldMapping.put("type", "keyword");

        textFieldMapping = new HashMap<>();
        textFieldMapping.put("type", "text");

        ikFieldMapping = new HashMap<>();
        ikFieldMapping.put("type", "text");
        ikFieldMapping.put("analyzer", "ik_max_word");
        ikFieldMapping.put("search_analyzer", "ik_smart");

        dateFieldMapping = new HashMap<>();
        dateFieldMapping.put("type", "date");
    }

    public static Map<String, Object> keywordMapping() {
        return keywordFieldMapping;
    }

    public static Map<String, Object> textMapping() {
        return textFieldMapping;
    }

    public static Map<String, Object> ikMapping() {
        return ikFieldMapping;
    }

    public static Map<String, Object> dateMapping() {
        return dateFieldMapping;
    }
}
