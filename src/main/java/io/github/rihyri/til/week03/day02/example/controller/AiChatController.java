package io.github.rihyri.til.week03.day02.example.controller;

import io.github.rihyri.til.week03.day02.example.dto.ChatRequest;
import io.github.rihyri.til.week03.day02.example.dto.ChatResponseDto;
import io.github.rihyri.til.week03.day02.example.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    // =========== 1. 일반 AI 질문 ===========
    @PostMapping("/chat")
    public ChatResponseDto chat (@RequestBody ChatRequest request) {
        return aiChatService.chat(request.getMessage());
    }


    // =========== 2. 히스토리를 포함한 대화 ===========
    @PostMapping("/chat/history")
    public ChatResponseDto chatWithHistory (@RequestBody ChatRequest request) {

        /*
         * 사용자가 전달한 질문과 대화 ID를 service로 전달한다.
         *
         * conversationId가 null이면 신규 대화
         * conversationId가 존재하면 기존 대화
         */
        return aiChatService.chatWithHistory(
                request.getMessage(),
                request.getConversationId()
        );
    }


    // =========== 3. 히스토리를 포함한 대화 ===========
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream (@RequestParam String question) {
        /*
         * produces = TEXT_EVENT_STREAM_VALUE
         * 응답을 text/event-stream 형태로 전달한다.
         */
        return aiChatService.chatStream(question);
    }


    // =========== 4. 전체 대화 삭제 ===========
    @DeleteMapping("/conversations")
    public ResponseEntity<Void> clearAll() {
        aiChatService.clearAll();
        return ResponseEntity.noContent().build();
    }
}
