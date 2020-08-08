package cn.denvie.elasticsearchspider.es.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 索引设置构造器。
 *
 * @author denvie
 * @date 2020/8/8
 */
public class SettingBuilder {
    private Map<String, String> settings;

    public SettingBuilder() {
        settings = new HashMap<>();
        settings.put("index.number_of_shards", "1");
        settings.put("index.number_of_replicas", "1");
    }

    public SettingBuilder number_of_shards(String number) {
        settings.put("index.number_of_shards", number);
        return this;
    }

    public SettingBuilder number_of_replicas(String number) {
        settings.put("index.number_of_replicas", number);
        return this;
    }

    public Map<String, String> build() {
        return settings;
    }
}
