package io.github.rihyri.til.week04.day03.example.controller;

import io.github.rihyri.til.week04.day03.example.dto.AnswerResponse;
import io.github.rihyri.til.week04.day03.example.dto.QuestionRequest;
import io.github.rihyri.til.week04.day03.example.response.ApiResponse;
import io.github.rihyri.til.week04.day03.example.service.AdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/day/advisor")
public class AdvisorController {

    private final AdvisorService advisorService;

    @PostMapping("/chat")
    public ApiResponse<AnswerResponse> chat (@RequestBody QuestionRequest request) {

        AnswerResponse result = advisorService.chat(request.getQuestion());

        return ApiResponse.ok(result);
    }
}
