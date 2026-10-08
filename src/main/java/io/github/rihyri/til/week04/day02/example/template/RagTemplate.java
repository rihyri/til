package io.github.rihyri.til.week04.day02.example.template;

public class RagTemplate {

    public static final String RAG_PROMPT_TEMPLATE = """
            다음 문서 내용을 참고하여 질문에 답변하세요.

            [문서]
            %s

            [질문]
            %s

            문서에 존재하지 않는 내용은 임의로 만들어내지 마세요.
            """;
}
