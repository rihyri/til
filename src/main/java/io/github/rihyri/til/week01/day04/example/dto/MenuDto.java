package io.github.rihyri.til.week01.day04.example.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class MenuDto {

    @Getter
    public static class Request {
        private Long id;
        private String name;
    }

    @Getter
    public static class Response {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class Parent {
        private Long id;
        private String name;

        // 하위 메뉴 DTO
        private List<Child> children;
    }

    @Getter
    @Builder
    public static class Child {
        private Long id;
        private String name;
    }
}
