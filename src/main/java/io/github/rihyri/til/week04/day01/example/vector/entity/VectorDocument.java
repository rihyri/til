package io.github.rihyri.til.week04.day01.example.vector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vector_documents")
public class VectorDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * 사용자가 업로드한 원본 파일명
     * 예) shopping-guide.txt
     */
    @Column(nullable = false)
    private String fileName;

    /*
     * 파일 전체 내용
     *
     * vector_store에는 잘린 chunk가 들어가지만
     * 이 테이블에는 원본 전체 내용을 저장한다.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /*
     * HTTP MultipartFile이 알려주는 방식
     *
     * 예) text/plain
     */
    @Column(nullable = false)
    private String contentType;

    /*
     * 이 문서를 몇 개의 Chunk로 나눴는지 기록
     */
    @Setter
    @Column(nullable = false)
    private int chunkCount;

    @Builder
    public VectorDocument(
            String fileName,
            String content,
            String contentType
    ) {
        this.fileName = fileName;
        this.content = content;
        this.contentType = contentType;

        // 처음 업로드됐을 때는 아직 Chunk를 만들지 않았으므로 0
        this.chunkCount = 0;
    }
}
