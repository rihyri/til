package io.github.rihyri.til.week05.day04.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("io.github.rihyri.til.week05.day04.example.user")
@EnableJpaRepositories("io.github.rihyri.til.week05.day04.example.user")
public class Week5Day04Application {

    public static void main (String[] args) {
        SpringApplication.run(Week5Day04Application.class, args);
    }
}
