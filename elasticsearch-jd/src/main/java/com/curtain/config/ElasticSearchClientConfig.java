package com.curtain.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：Curtain
 * @date ：Created in 2021/3/12 11:49
 * @description：1.找对象
 *              2.放到Sring中待用
 *              3.如果是Srpingboot就先解析源码
 *                  xxxxAutoConfiguration xxxxProperties
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.elasticsearch.rest") 
public class ElasticSearchClientConfig {
    
    private String host;
    private int port;
    private String scheme;
    
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
        return client;
    }
}
