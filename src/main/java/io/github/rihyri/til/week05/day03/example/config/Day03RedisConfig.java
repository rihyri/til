package io.github.rihyri.til.week05.day03.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class Day03RedisConfig {

    // Spring Boot가 자동 구성한 StringRedisTemplate을 사용한다.
    // @EnabledScheduling은 Write-back 작업을 주기적으로 실행하기 위한 설정이다.
}
