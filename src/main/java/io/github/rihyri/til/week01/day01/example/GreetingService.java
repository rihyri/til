package io.github.rihyri.til.week01.day01.example;

import org.springframework.stereotype.Service;

// Bean으로 등록하고 관리
@Service
public class GreetingService {

    // Controller에서 사용할 간단한 비즈니스 로직
    public String hello(String name) {
        return "Hello, " + name + "!";
    }
}
