package io.github.rihyri.til.week05.day01.example.category.controller;
import io.github.rihyri.til.common.response.ApiResponse;
import io.github.rihyri.til.week05.day01.example.category.dto.CategoryRequest;
import io.github.rihyri.til.week05.day01.example.category.dto.CategoryResponse;
import io.github.rihyri.til.week05.day01.example.category.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private static final String CACHE_KEY = "categoryCache";

    private final CategoryService categoryService;


    // 일반 조회: 요청할 때마다 DB 조회
    @GetMapping
    public ApiResponse<List<CategoryResponse>> findAll() {
        return ApiResponse.ok(categoryService.findAll());
    }

    // 세션 캐싱 조회
    @GetMapping("/cached")
    public ApiResponse<List<CategoryResponse>> findCached (HttpSession session) {

        // 1. 세션에 저장된 카테고리가 있는지 확인
        @SuppressWarnings("unchecked")
        List<CategoryResponse> cached = (List<CategoryResponse>) session.getAttribute(CACHE_KEY);

        if (cached != null) {
            // 2. 저장된 데이터가 있다면 DB 조회 없이 반환
            return ApiResponse.ok(cached);
        }

        // 3. 데이터가 없다면 DB 조회 후 세션에 저장
        List<CategoryResponse> categories = categoryService.findAll();

        session.setAttribute(CACHE_KEY, categories);

        return ApiResponse.ok(categories);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> create (
            @Valid @RequestBody CategoryRequest request,
            HttpSession session
    ) {

        categoryService.create(request);

        // 현재 사용자의 세션 캐시만 삭제
        session.removeAttribute(CACHE_KEY);

        return ApiResponse.ok();
    }
}
