package io.github.rihyri.til.week04.day02.example.service;

import io.github.rihyri.til.week04.day02.example.dto.AnswerResponse;
import io.github.rihyri.til.week04.day02.example.dto.RagResponse;
import io.github.rihyri.til.week04.day02.example.dto.SearchSummaryResponse;
import io.github.rihyri.til.week04.day02.example.dto.SimilaritySearchResponse;
import io.github.rihyri.til.week04.day02.example.exception.DomainException;
import io.github.rihyri.til.week04.day02.example.exception.DomainExceptionCode;
import io.github.rihyri.til.week04.day02.example.template.RagTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    // 실제 LLM에게 질문을 전달하는 객체
    private final ChatClient chatClient;

    // Embedding 된 문서를 저장하고 유사도 검색을 수행하는 객체
    private final VectorStore vectorStore;

    /*
     *  1. 가장 기본적인 RAG 질문
     */
    public AnswerResponse ask (String question) {

        /*
         *  Step 1.
         *  사용자 질문과 비슷한 Document를 VectorStore에서 찾는다.
         *
         *  최대 5개 검색, similarityThreshold = 0.8
         */
        List<Document> relevantDocs = searchDocuments(question, 5, 0.8);

        /*
         *  검색된 문서가 하나도 없다면 LLM에게 질문을 넘기지 않는다.
         *
         *  근거 문서가 없는데 LLM에게 답변을 요청하면 모델이 자신의 지식으로 답변할 수도 있기 때문이다.
         */
        if (relevantDocs.isEmpty()) {
            throw new DomainException(DomainExceptionCode.NOT_FOUND_DOCUMENT);
        }

        /*
         *  Step 2.
         *
         *  검색한 Document들과 질문을 LLM에게 전달하여 답변을 만든다.
         */
        String answer = generationAnswer(question, relevantDocs);

        /*
         *  Step 3.
         *
         *  최종 문자열을 Response DTO로 변환한다.
         */
        return AnswerResponse.builder()
                .answer(answer)
                .build();

    }


    /*
     *  2. 답변 + 출처 반환
     */
    public RagResponse askWithSource (String question) {

        /*
         *  기본 ask()와 마찬가지로 먼저 관련 문서를 검색한다.
         */
        List<Document> docs = searchDocuments(question, 5, 0.8);

        if (docs.isEmpty()) {
            throw new DomainException(DomainExceptionCode.NOT_FOUND_DOCUMENT);
        }

        String answer = generationAnswer(question, docs);

        /*
         *  이번 API에서는 답변뿐 아니라 "어떤 문서를 참고했는지"도 반환한다.
         *
         *  따라서 List<Document>를 list<DocumentSource> 형태로 변환한다.
         */
        List<RagResponse.DocumentSource> sources = docs.stream()
                // Document 하나를 DocumentSource DTO 하나로 변환
                .map(doc ->
                        RagResponse.DocumentSource.builder()
                                // Metadata 안에 저장되어 있는 원본 파일 이름을 가져온다.
                                .filename(
                                        (String) doc.getMetadata().get("filename")
                                )
                                // VectorStore에서 사용하는 Document ID
                                .documentId(doc.getId())
                                // 문서 전체를 보여주면 너무 길기 때문에 앞의 최대 100글자만 보여준다.
                                .preview(
                                        doc.getText().substring(0, Math.min(doc.getText().length(), 100))
                                )
                                .build()
                        ).toList();

        return RagResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }


    /*
     *  3. 특정 문서 안에서만 질문
     */
    public AnswerResponse askInDocument (String question, String documentId) {

        /*
         *  일반 검색과 가장 큰 차이점 : documentid를 이용하여 특정 문서에 속한 Chunk만 검색한다.
         */
        List<Document> relevantDocs = searchDocumentsWithFilter(question, documentId, 3);

        if (relevantDocs.isEmpty()) {
            throw new DomainException(DomainExceptionCode.NOT_FOUND_DOCUMENT);
        }

        /*
         *  이번에는 system Prompt도 사용한다.
         *
         *  LLM에게 "제공된 문서 내용만 사용하라"라는 역할/규칙을 먼저 지정한다.
         */
        String answer = chatClient.prompt()
                .system("""
                            당신은 전문 문서 기반 응답 시스템입니다.
                            제공된 문서 내용만 사용하세요.
                        """)
                /*
                 * 실제 사용자 질문에는
                 * 검색된 문서 + Context + 사용자 질문을 함께 전달한다.
                 */
                .user(
                        String.format(
                                RagTemplate.RAG_PROMPT_TEMPLATE,
                                combineDocuments(relevantDocs),
                                question
                        )
                )
                .call()
                .content();

        return AnswerResponse.builder()
                .answer(answer)
                .build();
    }


    /*
     *  4. 검색 결과 요약
     */
    public SearchSummaryResponse getSearchSummary (String query) {

        /*
         *  먼저 VectorStore에서 검색어와 관련된 문서들을 가져온다.
         */
        List<Document> docs = searchDocuments(query, 5, 0.7);

        /*
         *  여기서는 일반적인 질문 답변이 목적이 아니다.
         *
         *  검색된 문서들을 하나로 합친 후
         *  "한 문장으로 요약해줘"라고 LLM에게 요청한다.
         */
        String summary = chatClient.prompt()
                .user(
                        "다음 검색 결과들을 한 문장으로 요약해줘: \n" + combineDocuments(docs)
                )
                .call()
                .content();

        return SearchSummaryResponse.builder()
                .query(query)
                .summary(summary)
                .build();
    }

    // =================
    /*
     *  5. VectorStore 유사도 검색
     */
    public List<Document> searchDocuments (String query, int topK, double threshold) {

        // 질문을 Embedding한 뒤 VectorStore에서 유사한 Document를 찾는다.
        return vectorStore.similaritySearch(

                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .build()
        );
    }


    /*
     *  6. 특정 Document만 검색
     */
    public List<Document> searchDocumentsWithFilter (String query, String documentId, int topK) {

        return vectorStore.similaritySearch(

                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        /*
                         *  Metadata Filter
                         *
                         *  VectorStore 전체를 검색하는 것이 아니라
                         *  document_id가 전달받은 documentId와 일치하는 Document만 대상으로 검색한다.
                         */
                        .filterExpression(
                                "document_id == '" + documentId + "'"
                        )
                        .build()
        );
    }


    /*
     *  7. Document → API Response 반환
     */
    public SimilaritySearchResponse toSearchResponse (String query, List<Document> documents) {

        /*
         *  VectorStore에서 받은 Document는 그대로 내보내기보다는
         *  Response DTO로 변환한다.
         */
        List<SimilaritySearchResponse.SearchResult> results =
                documents.stream()
                        .map(doc ->
                                SimilaritySearchResponse.SearchResult.builder()
                                        .id(doc.getId())
                                        // 실제 검색된 Chunk 내용
                                        .content(doc.getText())
                                        // filename, document_id 등의 정보
                                        .metadata(doc.getMetadata())
                                        .build()
                        )
                        .toList();

        return SimilaritySearchResponse.builder()
                .query(query)
                .resultCount(results.size())
                .results(results)
                .build();
    }


    /*
     *  8. 검색 문서를 사용해 LLM 답변 생성
     */
    private String generationAnswer (String question, List<Document> docs) {

        // 여러 개의 Document를 하나의 문자열 Context로 합친다.
        String combinedDocuments = combineDocuments(docs);

        /*
         *  Prompt 구조
         *
         *  검색된 문서 + 사용자 질문을 LLM에게 전달한다.
         */
        return chatClient.prompt()
                .user(
                        String.format(RagTemplate.RAG_PROMPT_TEMPLATE, combinedDocuments, question)
                )
                .call()
                .content();
    }


    /*
     *  9. 여러 Document 내용을 하나로 합치기
     */
    private String combineDocuments (List<Document> documents) {

        return documents.stream()
                // Document 하나마다 '[파일명]: 내용' 형태로 변경한다.
                .map(doc ->
                        String.format(
                                "[%s]: %s",
                                doc.getMetadata()
                                        .getOrDefault("filename", "unknown"),
                                doc.getText()
                        )
                )
                // 여러 문서를 구분자를 넣어서 하나의 String으로 합친다.
                .collect(
                        Collectors.joining("\n\n==\n\n")
                );
    }
}
