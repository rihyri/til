package io.github.rihyri.til.week05.day04.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.web.ErrorResponse;
import java.util.*;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private static final int MAX_SESSIONS = 1;

    @Bean
    public SecurityFilterChain securityFilterChain
            (HttpSecurity http, SessionRegistry sessionRegistry, SecurityContextRepository securityContextRepository) throws Exception {

        http
                /*
                 *  학습을 위해 CSRF 방어를 비활성화한다.
                 *
                 *  세션 쿠키 기반 인증에서 CSRF 방어는 중요하다.
                 *  실제 서비스에서는 무조건 비활성화하면 안 된다.
                 */
                .csrf(AbstractHttpConfigurer::disable)
                /*
                 *  로그인하지 않은 사용자가 보호된 API에 접근할 때
                 *  요청을 별도 세션에 저장하지 않는다.
                 */
                .requestCache(cache -> cache.disable())
                // 인증 정보를 읽고 저장하는 담당 객체를 지정한다.
                .securityContext(context -> context
                        .securityContextRepository(
                                securityContextRepository
                        )
                )
                // 필요한 경우에만 HTTP 세션을 생성한다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )
                // 동시에 여러 번 로그인한 경우 만료 처리된 세션의 요청을 검사한다.
                .addFilterAt(
                        new ConcurrentSessionFilter(
                                sessionRegistry,
                                event -> writeError (
                                        event.getResponse(),
                                        ErrorCode.UNAUTHORIZED_ACCESS
                                )
                        ),
                        ConcurrentSessionFilter.class
                )
                // URL별 접근 권한 설정.
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인은 인증 없이 허용
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()
                        // 관리 기능은 ADMIN만 접근 가능
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 나머지 API는 로그인 필요
                        .requestMatchers("/api/**")
                        .authenticated()
                        // SecurityFilterChain에서는 그 밖의 요청도 인증이 필요하다.
                        .anyRequest().authenticated()
                )
                // 직접 JSON 로그인 API를 구현하므로 기존 로그인 폼을 사용하지 않는다.
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증도 사용하지 않는다.
                .httpBasic(AbstractHttpConfigurer::disable)
                // Spring Security에서 발생한 인증/인가 실패 처리
                .exceptionHandling(ex -> ex
                        // 인증 실패
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        writeError(
                                                response,
                                                ErrorCode.LOGIN_REQUIRED
                                        )
                        )
                        // 인가 실패 : 로그인했지만 권한 부족
                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        writeError(
                                                response,
                                                ErrorCode.UNAUTHORIZED_ACCESS
                                        )
                        )
                );

        return http.build();
    }

    // 인증 성공 후 세션을 처리하는 전략
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(SessionRegistry sessionRegistry) {

        // 1. 동시에 로그인 개수를 관리한다.
        ConcurrentSessionControlAuthenticationStrategy concurrent =
                new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);

        concurrent.setMaximumSessions(MAX_SESSIONS);

        /*
         *  false : 새 로그인을 허용하고 기존 세션을 만료 대상으로 표시
         *  true : 최대 세션 수에 도달하면 새로운 로그인을 거부
         */
        concurrent.setExceptionIfMaximumExceeded(false);

        /*
         *  2. 로그인 성공 시 세션 ID를 변경한다.
         *  세션 고정 공격(Session Fixation)을 방어하기 위한 전략이다.
         */
        ChangeSessionIdAuthenticationStrategy changeId = new ChangeSessionIdAuthenticationStrategy();

        // 3. 로그인 세션을 SessionRegistry에 등록한다.
        RegisterSessionAuthenticationStrategy register = new RegisterSessionAuthenticationStrategy(sessionRegistry);

        // 각 전략을 순서대로 실행한다.
        return new CompositeSessionAuthenticationStrategy(
                List.of(concurrent, changeId, register)
        );
    }

    // 인증 완료 정보를 HTTP 세션에 저장하는 객체
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // 기본 로그인 과정에서 사용할 AuthenticationManger
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 비밀번호를 BCrypt 방식으로 해시한다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Security 필터에서 발생한 오류를 JSON으로 응답한다.
    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.fail(errorCode.getMessage())
        );
    }
}
