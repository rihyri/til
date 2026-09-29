package io.github.rihyri.til.week03.day01.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        // Spring이 주입한 Builder로 ChatClient 생성
        return builder.build();
    }
}
