package com.bet99.exercise.jobsearch.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class ApplicationConfig {

    @Value("${solr.base.url:http://localhost:8983/solr}")
    private String solrBaseUrl;

    @Value("${solr.collection:jobtitles}")
    private String solrCollection;

    @Value("${solr.connection.timeout:10000}")
    private int connectionTimeout;

    @Value("${solr.request.timeout:30000}")
    private int requestTimeout;

    @Value("${solr.max.connections:200}")
    private int maxConnections;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;


    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrBaseUrl + "/" + solrCollection)
                .withConnectionTimeout(connectionTimeout)
                .withSocketTimeout(requestTimeout)
                .allowCompression(true)
                .build();
    }



    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        var config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        var factory = new JedisConnectionFactory(config);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}