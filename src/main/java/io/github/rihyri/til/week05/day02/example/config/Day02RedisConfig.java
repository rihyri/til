package io.github.rihyri.til.week05.day02.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class Day02RedisConfig {

    @Bean("day02RedisTemplate")
    public RedisTemplate<String, String> day02RedisTemplate (RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Spring Boot가 관리하는 Redis 연결 정보를 사용한다.
        template.setConnectionFactory(connectionFactory);

        // Key와 Value를 읽기 쉬운 문자열 형태로 저장한다.
        StringRedisSerializer serializer = new StringRedisSerializer();

        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }
}
