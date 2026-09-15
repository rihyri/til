package io.github.rihyri.til.week01.day01.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LessonApplication {

    public static void main(String[] args) {

        // Spring Boot 애플리케이션 실행
        // Spring Container를 생성하고 필요한 설정을 초기화한다.
        SpringApplication.run(LessonApplication.class, args);
    }
}
