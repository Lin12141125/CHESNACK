package com.chesnake.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置（优化8）：
 * - 客户端懒连接：ES 未启动不影响应用启动，查询失败自动降级 MySQL LIKE
 * - 通过 chesnake.elasticsearch.enabled=false 可整体关闭
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "chesnake.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient(
            @Value("${chesnake.elasticsearch.hosts:http://localhost:9200}") String hosts) {
        String[] urls = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[urls.length];
        for (int i = 0; i < urls.length; i++) {
            httpHosts[i] = HttpHost.create(urls[i].trim());
        }
        return RestClient.builder(httpHosts).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient, ObjectMapper objectMapper) {
        ElasticsearchTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        return new ElasticsearchClient(transport);
    }
}
