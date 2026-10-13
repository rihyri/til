package io.github.rihyri.til.week05.day01.example.auth.controller;

import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.common.response.ApiResponse;
import io.github.rihyri.til.week05.day01.example.auth.dto.LoginRequest;
import io.github.rihyri.til.week05.day01.example.auth.dto.LoginResponse;
import io.github.rihyri.til.week05.day01.example.auth.dto.SignUpRequest;
import io.github.rihyri.til.week05.day01.example.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final String LOGIN_MEMBER_ID = "loginMemberId";

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signUp (@Valid @RequestBody SignUpRequest request) {

        authService.signUp(request);
        return ApiResponse.ok();
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login (@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {

        // 1. 이메일, 비밀번호 검증
        LoginResponse response = authService.login(request);

        // 2. 기존 세션이 있다면 무효화 (세션 고정 공격 방어)
        HttpSession oldSession = servletRequest.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        // 3. 새 세션 생성 후 회원 ID 저장
        HttpSession newSession = servletRequest.getSession(true);
        newSession.setAttribute(LOGIN_MEMBER_ID, response.memberId());

        return ApiResponse.ok(response);
    }

    // 현재 로그인 상태 확인
    @GetMapping("/status")
    public ApiResponse<LoginResponse> status (HttpServletRequest request) {

        // 1. 기존 세션 조회 (없다면 새로 생성하지 않음)
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        // 2. 세션에 로그인 회원 정보가 있는지 확인
        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);

        if (memberId == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        return ApiResponse.ok(authService.getLoginInfo(memberId));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout (HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        // 기존 세션이 있다면 무효화
        if (session != null) {
            session.invalidate();
        }

        return ApiResponse.ok();
    }
}
