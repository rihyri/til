package io.github.rihyri.til.week03.day02.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {

        // Spring AI가 제공하는 Builder를 이용하여 ChatClient 생성
        return builder
                .defaultSystem("""
                        당신은 친절한 개발 학습 도우미입니다.
                        사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요
                        어려운 개념은 간단한 에시를 함께 설명해주세요.
                        """)
                .build();
    }
}
