package io.github.rihyri.til.week04.day01.example.vector.config;

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
    public VectorStore vectorStore (DataSource dataSource, @Qualifier("googleGenAiTextEmbedding")
    EmbeddingModel embeddingModel) {

        /*
         * pgVectorStore는 JDBC를 이용해서 PostgreSQL과 통신한다.
         * 따라서 DataSource를 이용해서 JdbcTemplate를 만든다.
         */
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        /*
         * Spring AI가 제공하는 PgVectorStore 생성
         *
         * 여기에서
         * EmbeddingModel + PostgreSQL
         * 두 가지가 연결된다.
         */
        return PgVectorStore.builder(
                jdbcTemplate,
                embeddingModel
        )
        /*
         * DB의 embedding vector(3072) 와 반드시 맞아야 한다.
         */
        .dimensions(3072)
        /*
         * Vector 사이의 거리를 Cosine Distance 방식으로 계산한다.
         */
        .distanceType(
                PgVectorStore.PgDistanceType.COSINE_DISTANCE
        )
        /*
         * 많은 Vector 중에서 가까운 Vector를
         * 빠르게 찾기 위한 인덱스 방식
         */
        .indexType(
                PgVectorStore.PgIndexType.HNSW
        )
        /*
         * 테이블은 Flyway에서 직접 만들 것이므로 false
         */
        .initializeSchema(false)
        /*
         * vector_store 테이블을 사용할 것을 지정
         */
        .vectorTableName("vector_store")
        .build();
    }
}
