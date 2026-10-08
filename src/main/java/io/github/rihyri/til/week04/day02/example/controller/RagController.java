package io.github.rihyri.til.week04.day02.example.controller;

import io.github.rihyri.til.week02.day01.example.global.response.ApiResponse;
import io.github.rihyri.til.week04.day02.example.dto.*;
import io.github.rihyri.til.week04.day02.example.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    /*
     *  1. 일반 RAG 질문
     *
     *  관련 문서를 검색한 후 LLM이 답변을 생성한다.
     *  반환: 답변만 반환
     */
    @PostMapping("/ask")
    public ApiResponse<AnswerResponse> ask (@RequestBody QuestionRequest request) {
        return ApiResponse.ok(ragService.ask(request.getQuestion()));
    }


    /*
     *  2. 출처 포함 RAG 질문
     *
     *  /ask와 동일하게 답변을 생성하지만 참고한 문서 정보까지 반환한다.
     *  반환: answer, sources
     */
    @PostMapping("/ask-with-source")
    public ApiResponse<RagResponse> askWithSource (@RequestBody QuestionRequest request) {
        return ApiResponse.ok(ragService.askWithSource(request.getQuestion()));
    }

    /*
     *  3. 특정 문서 안에서만 질문
     *
     *  전체 VectorStore가 아니라 URL로 전달받은 documentId 문서만 검색한다.
     */
    @PostMapping("/ask-in-document/{documentId}")
    public ApiResponse<AnswerResponse> askInDocument (@PathVariable String documentId, @RequestBody QuestionRequest request) {
        return ApiResponse.ok(ragService.askInDocument(request.getQuestion(), documentId));
    }

    /*
     *  4. 유사도 검색 결과 확인
     *
     *  여기서는 LLM에게 답변을 요청하지 않는다.
     *  VectorStore 검색 결과 자체를 반환한다.
     */
    @GetMapping("/search")
    public ApiResponse<SimilaritySearchResponse> search (@RequestParam String query, @RequestParam(defaultValue = "5") int topK) {

        /*
         *  threshold = 0.0
         *
         *  실습에서는 검색 결과 자체를 확인하기 위해 낮은 Threshold를 검새한다.
         */
        List<Document> docs = ragService.searchDocuments(query, topK, 0.0);

        return ApiResponse.ok(
                ragService.toSearchResponse(query, docs)
        );
    }


    /*
     *  5. 검색 결과 요약
     *
     *  관련 Document를 VectorStore에서 검색한 뒤, 검색 결과를 LLM에게 전달하여 한 문장으로 요약한다.
     */
    @GetMapping("/search-summary")
    public ApiResponse<SearchSummaryResponse> getSummary (@RequestParam String query) {
        return ApiResponse.ok(ragService.getSearchSummary(query));
    }
}
