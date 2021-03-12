package com.curtain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ElasticsearchJdApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchJdApplication.class, args);
    }

}
