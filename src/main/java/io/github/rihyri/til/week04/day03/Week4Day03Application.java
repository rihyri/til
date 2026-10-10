package io.github.rihyri.til.week04.day03;

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude = PgVectorStoreAutoConfiguration.class
)
public class Week4Day03Application {

    public static void main (String[] args) {
        SpringApplication.run(Week4Day03Application.class, args);
    }
}
