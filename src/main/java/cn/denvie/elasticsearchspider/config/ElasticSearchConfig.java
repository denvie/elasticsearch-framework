package cn.denvie.elasticsearchspider.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ElasticSearchConfig {

    private List<String> hosts = Arrays.asList("localhost:9200");

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] httpHosts = new HttpHost[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            String[] splits = hosts.get(i).split(":");
            httpHosts[i] = new HttpHost(splits[0].trim(),
                    Integer.parseInt(splits[1].trim()), "http");
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }

}
