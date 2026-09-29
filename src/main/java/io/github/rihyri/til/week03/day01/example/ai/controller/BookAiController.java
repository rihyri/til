package io.github.rihyri.til.week03.day01.example.ai.controller;

import io.github.rihyri.til.week03.day01.example.ai.dto.BookAnswerResponse;
import io.github.rihyri.til.week03.day01.example.ai.dto.BookQuestionRequest;
import io.github.rihyri.til.week03.day01.example.ai.service.BookAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books/ai")
@RequiredArgsConstructor
public class BookAiController {

    private final BookAiService bookAiService;

    @PostMapping("/ask")
    public BookAnswerResponse ask(@RequestBody BookQuestionRequest request) {
        return bookAiService.ask(request);
    }
}
