package io.github.rihyri.til.week03.day03.example.controller;

import io.github.rihyri.til.week03.day03.example.dto.ChatRequest;
import io.github.rihyri.til.week03.day03.example.dto.ContextChatResponse;
import io.github.rihyri.til.week03.day03.example.service.PersistenceChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final PersistenceChatService chatService;

    @PostMapping
    public ContextChatResponse chat (@RequestBody ChatRequest request) {

        return chatService.chat(
                request.conversationId(), request.message()
        );
    }
}
