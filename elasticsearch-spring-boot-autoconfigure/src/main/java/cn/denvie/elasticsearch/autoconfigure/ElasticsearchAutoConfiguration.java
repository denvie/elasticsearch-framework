/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.autoconfigure;

import cn.denvie.elasticsearch.service.ElasticsearchService;
import cn.denvie.elasticsearch.service.impl.ElasticsearchServiceImpl;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch auto configuration.
 *
 * @author denvie
 * @since 2020/8/22
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient(ElasticsearchProperties properties) {
        HttpHost[] httpHosts = new HttpHost[properties.getHosts().size()];
        for (int i = 0; i < properties.getHosts().size(); i++) {
            String[] splits = properties.getHosts().get(i).split(":");
            httpHosts[i] = new HttpHost(splits[0].trim(),
                    Integer.parseInt(splits[1].trim()), properties.getScheme());
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchService elasticsearchService(RestHighLevelClient restHighLevelClient,
                                                     ElasticsearchProperties properties) {
        ElasticsearchService elasticsearchService = new ElasticsearchServiceImpl(
                restHighLevelClient, properties.getTimeoutSeconds());
        return elasticsearchService;
    }
}
