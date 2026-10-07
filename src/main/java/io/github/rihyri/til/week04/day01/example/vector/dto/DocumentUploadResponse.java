package io.github.rihyri.til.week04.day01.example.vector.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentUploadResponse  {

    private String documentId;

    private String fileName;

    private int chunkCount;
}
