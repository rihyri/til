package io.github.rihyri.til.week04.day02.example.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore (JdbcTemplate jdbcTemplate, @Qualifier("googleGenAiTextEmbedding")EmbeddingModel embeddingModel) {

        /*
         *  VectorStore의 역할
         *
         *  1. Document 저장
         *  2. Document를 Embedding Vector로 변환
         *  3. 질문과 비슷한 Vector 검색
         */
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .build();
    }
}
