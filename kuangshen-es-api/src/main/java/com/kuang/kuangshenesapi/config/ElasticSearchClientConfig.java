package com.kuang.kuangshenesapi.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 狂神的Spring两步骤
// 1.找对象
// 2.放到spring中
// 3.如果是springboot，先分析源码
// xxxx AutoConfiguration xxxProperfile
@Configuration
public class ElasticSearchClientConfig {
    
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http"))
        );
        return client;
    }
    
}
