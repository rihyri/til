package io.github.rihyri.til.week04.day01.example.vector.service;

import io.github.rihyri.til.week04.day01.example.vector.dto.DocumentUploadResponse;
import io.github.rihyri.til.week04.day01.example.vector.dto.SimilaritySearchResponse;
import io.github.rihyri.til.week04.day01.example.vector.entity.VectorDocument;
import io.github.rihyri.til.week04.day01.example.vector.repository.VectorDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VectorDocumentService {

    // 원본 문서 저장용 Repository
    private final VectorDocumentRepository vectorDocumentRepository;

    // 실제 Vector 저장/검색을 담당하는 Spring AI 객체
    private final VectorStore vectorStore;

    /*
     * 1. 문서 업로드
     */
    @Transactional
    public DocumentUploadResponse uploadDocument (MultipartFile file) throws IOException {

        /*
         * MultipartFile에서 파일 정보를 꺼낸다.
         *
         * 예)
         * fileName = shopping-guide.txt
         * contentType = text/plain
         */
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        /*
         * 이번 예제에서는 TXT 파일만 다룬다고 가정한다.
         * 파일의 byte[]를 UTF-8 문자열로 변환한다.
         */
        String content = new String(
                file.getBytes(), StandardCharsets.UTF_8
        );

        /*
         * STEP. 1
         * 원본 문서를 vector_documents에 저장
         */
        VectorDocument vectorDocument = VectorDocument.builder()
                .fileName(fileName)
                .content(content)
                .contentType(contentType)
                .build();

        VectorDocument savedDocument = vectorDocumentRepository.save(vectorDocument);

        /*
         * 지금 DB에서는 이런 상태다.
         *
         * vector_documents
         *
         * id   : UUID
         * filename : shopping-guide.txt
         * content  : 전체 내용
         *
         * 아직 Vector는 없다.
         */

        /*
         * STEP 2
         * 긴 문서를 작은 Chunk로 분리
         */
        List<Document> chunks = createChunks(content, savedDocument);

        /*
         * 예를 들어 하나의 문서가
         *
         * Chunk 1
         * Chunk 2
         * Chunk 3
         *
         * 으로 나눠졌다면
         *
         * chunks.size() == 3
         */
        savedDocument.setChunkCount(chunks.size());

        /*
         * STEP 3
         * Embedding 생성 + Vector DB 저장
         */

        /*
         * 우리가 직접
         * embeddingModel.embed(...)를 호출하지 않는다.
         *
         * VectorStore가 내부에서
         *
         * 1. 각 Document의 Text를 가져오고
         * 2. EmbeddingModel을 호출하고
         * 3. Vector를 생성하고
         * 4. PostgreSQL vector_store에 저장한다.
         */
        vectorStore.add(chunks);

        return DocumentUploadResponse.builder()
                .documentId(savedDocument.getId().toString())
                .fileName(savedDocument.getFileName())
                .chunkCount(chunks.size())
                .build();
    }

    /*
     * 2. Chunk 생성
     */
    private List<Document> createChunks (String content, VectorDocument vectorDocument) {

        /*
         * 긴 문서를 일정 Token 크기로 잘라주는 객체
         */
        TextSplitter splitter = TokenTextSplitter.builder()
                // Chunk 하나의 목표 Token 크기
                .withChunkSize(500)
                // 너무 작은 Chunk 생성 방지
                .withMinChunkSizeChars(100)
                // 너무 짧은 내용은 Embeding 하지 않음
                .withMinChunkLengthToEmbed(5)
                // 비정상적으로 많은 Chunk 생성 방지
                .withMaxNumChunks(10000)
                // 구분 문자 유지
                .withKeepSeparator(true)
                .build();

        /*
         * 각 Chunk에 공통으로 넣어줄 Metadata
         *
         * document_id를 넣는 이유가 특히 중요하다.
         * 나중에 "이 문서에만 검색해줘" 라는 조건을 줄 수 있기 때문이다.
         */
        Map<String, Object> metadata = Map.of(
                "document_id", vectorDocument.getId().toString(),
                "filename", vectorDocument.getFileName(),
                "source", "user_upload"
        );

        /*
         * Spring AI의 Document
         *
         * 일반 JPA Entity가 아니다.
         *
         * Vector Store로 전달하기 위한
         * Spring AI 전용 데이터 객체라고 보면 된다.
         */
        Document document = new Document(content, metadata);

        /*
         * 하나의 긴 Document → 여러 개의 Document Chunk
         */
        return splitter.split(document);
    }

    /*
     * 3. 유사도 검색
     */
    public SimilaritySearchResponse search (UUID documentId, String query, int topK) {

        /*
         * searchRequest를 만드는 순간에는 아직 DB를 검색하지 않는다.
         * 검색 조건을 만드는 단계이다.
         */
        SearchRequest request = SearchRequest.builder()
                /*
                 * 사용자의 자연어 질문
                 *
                 * 예) "환불 기간이 어떻게 돼?"
                 * 이 query 역시 내부적으로 Embedding 된다.
                 */
                .query(query)
                /*
                 * 가장 비슷한 결과 몇 개를 가져올지
                 */
                .topK(topK)
                /*
                 * 특정 documentId에서 생성된 chunk만 검색한다.
                 *
                 * 즉, metadata의 document = ? 조건이다.
                 */
                .filterExpression(
                        "document_id == '"
                                    + documentId
                                    + "'"
                )
                .build();

        /*
         ** 실제 Vector Search
         *
         * 내부 흐름 :
         * query → EmbeddingModel → Query Vector
         * → pgvector → 저장된 Vector와 거리 계산 → Top K 반환
         */
        List<Document> documents = vectorStore.similaritySearch(request);

        /*
         * Spring AI Document를 API Response DTO로 반환한다.
         */
        List<SimilaritySearchResponse.SearchResult> results = documents.stream()
                .map(document ->
                        SimilaritySearchResponse.SearchResult
                                .builder()
                                .id(document.getId())
                                // 검색 결과의 원문 Chunk
                                .content(document.getText())
                                // document_id, filename 등의 정보
                                .metadata(
                                        document.getMetadata()
                                )
                                .build()
                ).toList();

        return SimilaritySearchResponse.builder()
                .query(query)
                .resultCount(results.size())
                .results(results)
                .build();
    }
}
