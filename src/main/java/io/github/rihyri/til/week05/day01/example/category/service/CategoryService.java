package io.github.rihyri.til.week05.day01.example.category.service;

import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.week05.day01.example.category.dto.CategoryRequest;
import io.github.rihyri.til.week05.day01.example.category.dto.CategoryResponse;
import io.github.rihyri.til.week05.day01.example.category.entity.Category;
import io.github.rihyri.til.week05.day01.example.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {

        // 1. DB에서 전체 카테고리 조회
        List<Category> categories = categoryRepository.findAll();

        // 2. ID로 응답 객체 찾을 수 있도록 Map 생성
        Map<Long, CategoryResponse> responseMap = new HashMap<>();

        for (Category category : categories) {
            responseMap.put(
                    category.getId(),
                    CategoryResponse.of(
                            category.getId(),
                            category.getName()
                    )
            );
        }

        List<CategoryResponse> roots = new ArrayList<>();

        // 3. 부모 카테고리와 자식 카테고리 연결
        for (Category category : categories) {

            CategoryResponse current = responseMap.get(category.getId());

            if (category.getParent() == null) {

                // 부모가 없다면 최상위 카테고리
                roots.add(current);
                continue;
            }

            Long parentId = category.getParent().getId();
            CategoryResponse parent = responseMap.get(parentId);

            if (parent != null) {
                parent.children().add(current);
            }
        }

        return roots;
    }

    @Transactional
    public void create(CategoryRequest request) {

        Category parent = null;

        // 1. 부모 ID가 전달되었다면 부모 카테고리 조회
        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND)
                    );
        }

        // 2. 새로운 카테고리 생성 및 DB 저장
        Category category = new Category(request.name(), parent);

        categoryRepository.save(category);
    }
}
