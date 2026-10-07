package io.github.rihyri.til.week04.day01.example;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
exclude = PgVectorStoreAutoConfiguration.class
)
public class Week4Day01Application {

    public static void main(String[] args) {

        // 현재 4주차 1일 실습 Application 실행
        SpringApplication.run(Week4Day01Application.class, args);
    }
}
