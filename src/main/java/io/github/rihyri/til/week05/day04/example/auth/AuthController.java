package io.github.rihyri.til.week05.day04.example.auth;

import io.github.rihyri.til.week05.day04.example.auth.dto.LoginRequest;
import io.github.rihyri.til.week05.day04.example.auth.dto.LoginResponse;
import io.github.rihyri.til.week05.day04.example.auth.dto.RegisterRequest;
import io.github.rihyri.til.week05.day04.example.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // ======================================
    // 회원가입
    // POST /api/day04/auth/register
    // ======================================
    @PostMapping("/auth/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ======================================
    // 로그인
    // POST /api/auth/login
    // ======================================
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response
    ) {

        LoginResponse result = authService.login(
                loginRequest, request, response
        );

        return ResponseEntity.ok(result);
    }

    // ======================================
    // 현재 로그인 상태 확인
    // GET /api/auth/me
    // ======================================
    @GetMapping("/auth/me")
    public ResponseEntity<LoginResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {

        /*
         *  Spring Security가 세션의 인증 정보를 확인하고,
         *  Authentication에 들어 있는 principal을 파라미터에 전달해준다.
         *
         *  직접 Redis에서 사용자를 조회할 필요가 없다.
         */
        return ResponseEntity.ok(LoginResponse.from(userDetails));
    }

    // ======================================
    // 일반 로그아웃
    // POST /api/auth/logout
    // ======================================
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        // 현재 요청과 연결된 세션을 무효화하고 SecurityContext를 정리한다.
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

        logoutHandler.logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }

    // ======================================
    // 관리자 강제 로그아웃
    // POST /api/admin/users/{userId}/logout
    // ======================================
    @PostMapping("/admin/users/{userId}/logout")
    public ResponseEntity<Void> forceLogout(@PathVariable Long userId) {

        // SecurityConfig에서 ADMIN만 허용했기 때문에/
        // 일반 USER 권한으로 호출하면 Controller에 도달하기 전에 거부된다.
        authService.forceLogout(userId);

        return ResponseEntity.noContent().build();
    }
}
