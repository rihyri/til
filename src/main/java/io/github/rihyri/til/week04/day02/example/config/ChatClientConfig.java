package io.github.rihyri.til.week04.day02.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {

        // Spring AI가 생성해준 ChatModel을 이용하여
        // ChatClient를 Bean으로 등록한다.
        return ChatClient.builder(chatModel)
                .build();
    }
}
