package io.github.rihyri.til.week04.day03.example.controller;

import io.github.rihyri.til.week04.day03.example.dto.AnswerResponse;
import io.github.rihyri.til.week04.day03.example.dto.QuestionRequest;
import io.github.rihyri.til.week04.day03.example.response.ApiResponse;
import io.github.rihyri.til.week04.day03.example.service.FunctionCallingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/function")
public class FunctionController {

    private final FunctionCallingService functionCallingService;

    // 1. 기본 Function Calling
    @PostMapping("/chat")
    public ApiResponse<AnswerResponse> chat (@RequestBody QuestionRequest request) {

        AnswerResponse result = functionCallingService.chat(request.getQuestion());

        return ApiResponse.ok(result);
    }

    // 2. System Prompt를 추가한 Function calling
    @PostMapping("/chat/system")
    public ApiResponse<AnswerResponse> chatWithSystem (@RequestBody QuestionRequest request) {

        AnswerResponse result = functionCallingService.chatWithSystemMessage(request.getQuestion());

        return ApiResponse.ok(result);
    }
}
