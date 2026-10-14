package io.github.rihyri.til.week05.day02.example.controller;

import com.oracle.svm.core.annotate.Delete;
import io.github.rihyri.til.week05.day02.example.dto.ThemeSessionResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/session")
public class ThemeSessionController {

    private static final String THEME_KEY = "selectedTheme";

    // 사용자가 선택한 테마를 세션에 저장한다.
    @PostMapping("/{theme}")
    public ThemeSessionResponse saveTheme (@PathVariable String theme, HttpSession session) {

        session.setAttribute(THEME_KEY, theme);

        return new ThemeSessionResponse(
                session.getId(), theme
        );
    }

    // 현재 세션에 저장된 테마를 조회한다.
    @GetMapping
    public ThemeSessionResponse findTheme (HttpSession session) {

        String theme = (String) session.getAttribute(THEME_KEY);

        return new ThemeSessionResponse(
                session.getId(), theme
        );
    }

    // 세션을 무효화하여 저장된 정보를 삭제한다.
    @DeleteMapping
    public String clearSession (HttpSession session) {

        session.invalidate();

        return "세션이 삭제되었습니다.";
    }
}
