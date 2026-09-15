package io.github.rihyri.til.week01.day01.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final GreetingService greetingService;

    // 생성자 주입
    // Spring Container가 관리하는 GreetingService Bean을 주입받는다.
    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    // GET /api/hello 요청을 처리한다.
    // name 값이 없으면 기본값으로 "Spring"을 사용한다.
    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "Spring") String name) {

        // 직접 로직을 처리하지 않고 Service에게 작업을 요청한다.
        return greetingService.hello(name);
    }
}
