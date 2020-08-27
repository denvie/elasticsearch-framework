/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.config;

import cn.denvie.elasticsearch.properties.ElasticsearchProperties;
import cn.denvie.elasticsearch.service.ElasticsearchService;
import cn.denvie.elasticsearch.service.impl.ElasticsearchServiceImpl;
import cn.denvie.elasticsearch.utils.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Elasticsearch配置。
 *
 * @author denvie
 * @since 2020/8/24
 */
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchConfig {
    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient(ElasticsearchProperties properties) {
        HttpHost[] httpHosts = new HttpHost[properties.getHosts().size()];
        for (int i = 0; i < properties.getHosts().size(); i++) {
            String[] splits = properties.getHosts().get(i).split(":");
            httpHosts[i] = new HttpHost(splits[0].trim(),
                    Integer.parseInt(splits[1].trim()), properties.getScheme());
        }
        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (properties.getPathPrefix() != null && properties.getPathPrefix().trim().length() > 0) {
            builder.setPathPrefix(properties.getPathPrefix().trim());
        }

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            // setup basic auth
            if (StringUtils.isNotBlank(properties.getUsername())
                    && StringUtils.isNotBlank(properties.getPassword())) {
                String credentialsString = properties.getUsername() + ":" + properties.getPassword();
                byte[] encodedBytes = Base64.getEncoder().encode(credentialsString.getBytes(StandardCharsets.UTF_8));
                String headerName = "Authorization";
                String headerValue = "Basic " + new String(encodedBytes, StandardCharsets.UTF_8);
                builder.setDefaultHeaders(new Header[]{new BasicHeader(headerName, headerValue)});
            }

            // setup logger
            if (properties.isEnableLogger()) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                httpClientBuilder.addInterceptorLast((HttpRequestInterceptor) interceptor);
                httpClientBuilder.addInterceptorLast((HttpResponseInterceptor) interceptor);
            }

            // setup timeout
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            if (properties.getConnectTimeout() > 0) {
                int timeout = properties.getConnectTimeout() * 1000;
                requestConfigBuilder.setConnectTimeout(Math.toIntExact(timeout));
                requestConfigBuilder.setConnectionRequestTimeout(Math.toIntExact(timeout));
            }
            if (properties.getSocketTimeout() > 0) {
                requestConfigBuilder.setSocketTimeout(Math.toIntExact(properties.getSocketTimeout() * 1000));
            }
            httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

            return httpClientBuilder;
        });

        return new RestHighLevelClient(builder);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchService elasticsearchService(RestHighLevelClient restHighLevelClient,
                                                     ElasticsearchProperties properties) {
        return new ElasticsearchServiceImpl(restHighLevelClient, properties.getRequestTimeoutSeconds());
    }
}
