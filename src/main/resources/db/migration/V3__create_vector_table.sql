-- 1. 원본 문서 관리

CREATE TABLE vector_documents
(
    id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 사용자가 업로드한 원본 파일명
    file_name   VARCHAR(255) NOT NULL,

    -- 원본 문서 전체 내용
    content     TEXT NOT NULL,

    -- text/plain 등의 Content-Type
    content_type VARCHAR(100) NOT NULL,

    metadata    TEXT,

    -- 이 문서가 몇 개의 chunk로 나뉘었는지 저장
    chunk_count     INTEGER NOT NULL DEFAULT 0,

    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 실제 Vector 검색용

CREATE TABLE vector_store
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 잘게 나눈 문서의 실제 텍스트
    content TEXT NOT NULL,

    -- document_id, filename 등을 저장
    metadata JSONB,

    -- Embedding Model이 생성한 Vector
    embedding vector(3072),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 파일 검색용
CREATE INDEX idx_documents_filename ON vector_documents(file_name);

-- JSON Metadata 검색용
CREATE INDEX idx_vector_store_metadata
ON vector_store USING gin(metadata);

