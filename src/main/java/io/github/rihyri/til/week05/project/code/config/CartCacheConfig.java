package io.github.rihyri.til.week05.project.code.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CartCacheConfig {

    /*
     *  RedisCacheManager를 Bean으로 등록한다.
     *
     *  Spring Cache는 CacheManager를 이용해 캐시를 조회하고, 저장하고, 삭제한다.
     *
     *  RedisConnectionFactory는 이미 설정된 Redis 연결 정보를 이용하는 객체이다.
     *
     *  Spring Boot의 Redis 자동 설정이 정상 작동한다면
     *  별도로 new RedisConnectionFactory(...)를 하지 않아도 된다.
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        /*
         *  Redis에 데이터를 어떤 방식으로 저장할지 설정한다.
         *
         *  defaultCacheConfig()는 기본 캐시 설정을 생성한다.
         *
         *  주요 기본값:
         *  - 캐시 키를 문자열로 직렬화
         *  - 캐시 값을 JDK 직렬화 방식으로 저장
         *  - 캐시 이름을 키의 접두어로 사용
         *  - TTL을 설정하지 않으면 기본적으로 무기한으로 보관
         */
        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        /*
                         *  Redis 캐시 데이터의 TTL을 5분으로 지정한다.
                         *
                         *  조회 결과가 Redis에서 저장된 후 5분이 지나면 자동으로 만료된다.
                         */
                        .entryTtl(Duration.ofMinutes(5))
                        /*
                         *  메서드 결과가 null이면 Redis에 캐싱하지 않는다.
                         *
                         *  다만 빈 List와 null은 다르다.
                         *  빈 List는 정상적인 데이터이므로 캐싱할 수 있다.
                         */
                        .disableCachingNullValues();

        /*
         *  CacheManager를 생성한다.
         *
         *  connectionFactory:
         *  실제 Redis 서버와 연결하는 데 사용한다.
         *
         *  cacheDefaults(config):
         *  위에서 생성한 TTL, 직렬화 설정 등을 기본 캐시 설정으로 적용한다.
         */
        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
