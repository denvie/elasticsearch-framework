/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * Elasticsearch properties.
 *
 * @author denvie
 * @since 2020/8/22
 */
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    /**
     * Elasticsearch主机，多个以","分隔
     */
    private List<String> hosts = Collections.singletonList("localhost:9200");
    /**
     * 连接协议
     */
    private String scheme = "http";
    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 30;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
