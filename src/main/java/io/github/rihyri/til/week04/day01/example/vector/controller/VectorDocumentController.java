package io.github.rihyri.til.week04.day01.example.vector.controller;

import io.github.rihyri.til.week04.day01.example.vector.dto.DocumentUploadResponse;
import io.github.rihyri.til.week04.day01.example.vector.dto.SimilaritySearchResponse;
import io.github.rihyri.til.week04.day01.example.vector.service.VectorDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vector-document")
public class VectorDocumentController {

    private final VectorDocumentService vectorDocumentService;

    /*
     * 파일 업로드
     *
     * POST
     * /api/vector-document/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentUploadResponse upload (@RequestParam("file")MultipartFile file) throws IOException {

        return vectorDocumentService.uploadDocument(file);
    }

    /*
     * Vector 유사도 검색
     *
     * GET
     * /api/vector-document/similarity
     */
    @GetMapping("/similarity")
    public SimilaritySearchResponse similaritySearch (
            @RequestParam UUID documentId,
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK
    ) {
        return vectorDocumentService.search(documentId, query, topK);
    }
}
