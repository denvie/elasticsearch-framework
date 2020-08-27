/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.properties;

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
     * 路径前缀
     */
    private String pathPrefix;
    /**
     * 是否开启 Request / Response 日志
     */
    private boolean enableLogger = true;
    /**
     * 连接超时
     */
    private int connectTimeout = 10;
    /**
     * 通信超时
     */
    private int socketTimeout = 5;
    /**
     * 单个请求超时时间（秒）
     */
    private int requestTimeoutSeconds = 30;
    /**
     * BasicAuth: 用户名
     */
    private String username;
    /**
     * BasicAuth: 密码
     */
    private String password;

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

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public boolean isEnableLogger() {
        return enableLogger;
    }

    public void setEnableLogger(boolean enableLogger) {
        this.enableLogger = enableLogger;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
