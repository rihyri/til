package io.github.rihyri.til.week04.day02.example;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = PgVectorStoreAutoConfiguration.class
)
public class Week4Day02Application {

    public static void main(String[] args) {

        // 현재 4주차 2일 실습 Application 실행
        SpringApplication.run(Week4Day02Application.class, args);
    }
}
