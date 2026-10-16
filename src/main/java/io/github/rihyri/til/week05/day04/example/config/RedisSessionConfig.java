package io.github.rihyri.til.week05.day04.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

@Configuration
@EnableRedisIndexedHttpSession
public class RedisSessionConfig {

    /*
     *  Spring Session이 세션 데이터를 직렬화할 때 사용할 RedisSerialize를 직접 등록한다.
     *
     *  springSessionDefaultRedisSerializer 라는 이름으로 등록하면
     *  Spring Session의 기본 Serializer를 교체할 수 있다.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {

        /*
         *  JDK 직렬화 방식을 사용한다.
         *
         *  getClass().getClassLoader()를 전달하면 현재 설정 클래스를 로딩한 ClassLoader를
         *  역직렬화 과정에서도 사용할 수 있다.
         *
         *  DevTools 환경에서 RestartClassLoader와 기본 ClassLoader가 충돌하는
         *  일부 문제를 예방하는 데 도움이 될 수 있다.
         */
        return new JdkSerializationRedisSerializer(getClass().getClassLoader());
    }

    /*
     *  SessionRegistry는 로그인한 사용자의 세션을
     *  확인하거나 동시 로그인 수를 관리하는 데 사용한다.
     *
     *  일반적인 메모리 기반 SessionRegistry가 아닌
     *  Spring Session의 Redis 세션 저장소와 연결한다.
     */
    @Bean
    public SessionRegistry sessionRegistry(RedisIndexedSessionRepository repository) {
        return new SpringSessionBackedSessionRegistry<>(repository);
    }
}
