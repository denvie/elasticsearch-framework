/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 索引设置构造器。
 *
 * @author denvie
 * @since 2020/8/8
 */
public class SettingBuilder {
    private Map<String, String> settings;

    public SettingBuilder() {
        settings = new HashMap<>();
        settings.put("index.number_of_shards", "1");
        settings.put("index.number_of_replicas", "1");
    }

    /**
     * 设置分片数。
     *
     * @param number 分片数
     * @return SettingBuilder
     */
    public SettingBuilder numberOfShards(String number) {
        settings.put("index.number_of_shards", number);
        return this;
    }

    /**
     * 设置副本数。
     *
     * @param number 副本数
     * @return SettingBuilder
     */
    public SettingBuilder number_of_replicas(String number) {
        settings.put("index.number_of_replicas", number);
        return this;
    }

    /**
     * 构建索引设置。
     *
     * @return 索引设置Map
     */
    public Map<String, String> build() {
        return settings;
    }
}
