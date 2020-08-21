/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Elasticsearch配置。
 *
 * @author denvie
 * @since 2020/8/4
 */
@Configuration
public class ElasticsearchConfig {
    @Value("${elasticsearch.hosts}")
    private List<String> hosts = Arrays.asList("localhost:9200");
    @Value("${elasticsearch.scheme}")
    private String scheme = "http";

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] httpHosts = new HttpHost[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            String[] splits = hosts.get(i).split(":");
            httpHosts[i] = new HttpHost(splits[0].trim(),
                    Integer.parseInt(splits[1].trim()), scheme);
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}
