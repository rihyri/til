package io.github.rihyri.til.week04.day03.example.config;

import io.github.rihyri.til.week04.day03.example.advisior.ChatMemoryAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient (ChatModel chatModel) {

        /*
         *  Step 1. 직접 만든 Advisor 객체를 생성
         *
         *  - "study-room" : 대화 기록을 구분하기 위한 ID
         *  - 10 : 최대 저장 메시지 개수
         */
        ChatMemoryAdvisor memoryAdvisor = new ChatMemoryAdvisor("study-room", 10);


        /*
         *  Step 2. ChatClient를 생성한다.
         */
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                    당신은 Java와 Spring 학습을 돕는 AI입니다.
                    설명은 초보자가 이해할 수 있도록 작성해주세요.
                    이전 대화가 제공되었다면 그 문맥을 참고해주세요.
                """)
            .defaultAdvisors(memoryAdvisor)
            .build();
    }
}
