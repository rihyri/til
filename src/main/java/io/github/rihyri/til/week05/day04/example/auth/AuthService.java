package io.github.rihyri.til.week05.day04.example.auth;

import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.week05.day04.example.auth.dto.LoginRequest;
import io.github.rihyri.til.week05.day04.example.auth.dto.LoginResponse;
import io.github.rihyri.til.week05.day04.example.auth.dto.RegisterRequest;
import io.github.rihyri.til.week05.day04.example.security.CustomUserDetails;
import io.github.rihyri.til.week05.day04.example.security.RedisUserSessionManager;
import io.github.rihyri.til.week05.day04.example.user.User;
import io.github.rihyri.til.week05.day04.example.user.UserRepository;
import io.github.rihyri.til.week05.day04.example.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository securityContextRepository;

    private final RedisUserSessionManager userSessionManager;


    //  ======================================
    //  1. 회원가입
    //  ======================================
    @Transactional
    public void register(RegisterRequest request) {

        // 이미 가입한 이메일인지 확인한다.
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 비밀번호 원문을 DB에 저장하면 안 된다.
        // BCrypt로 해시한 결과를 저장한다.
        String encodedPassword = passwordEncoder.encode(request.password());

        // 일반 회원은 USER 권한으로 가입한다.
        User user = new User(
                request.email(),
                encodedPassword,
                UserRole.USER
        );
    }

    //  ======================================
    //  2. 로그인
    //  ======================================
    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {

        /*
         *  STEP 1.
         *
         *  로그인에 필요한 이메일과 비밀번호를 인증 전용 객체에 담는다.
         *  이 단계에서는 아직 인증되지 않는다.
         */
        UsernamePasswordAuthenticationToken loginToken =
                UsernamePasswordAuthenticationToken.unauthenticated(
                            loginRequest.email(),
                            loginRequest.password()
                );

        Authentication authentication;

        /*
         *  STEP 2.
         *
         *  AuthenticationManager에게 인증을 요청한다.
         *
         *  내부적으로 DaoAuthenticationProvider가 CustomUserDetailService를 사용해 회원을 조회하고,
         *  PasswordEncoder로 비밀번호를 비교한다.
         */
        try {
            authentication = authenticationManager.authenticate(loginToken);
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        /*
         *  STEP 3.
         *
         *  인증 성공 후 세션 전략을 실행한다.
         *  - 동시 로그인 개수 확인
         *  - 세션 ID 변경
         *  - 세션 정보 등록
         */
        try {
            sessionStrategy.onAuthentication(
                    authentication, request, response
            );
        } catch (SessionAuthenticationException exception) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        /*
         *  STEP 4.
         *
         *  새로운 SecurityContext를 만든다.
         *
         *  SecurityContext는 현재 인증된 사용자 정보를 담아두는 객체이다.
         */
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 인증이 완료된 Authentication을 저장한다.
        context.setAuthentication(authentication);

        // 현재 요청에서 인증 정보를 사용할 수 있게 한다.
        SecurityContextHolder.setContext(context);

        /*
         *  STEP 5.
         *
         *  SecurityContext를 HTTP 세션에 저장한다.
         *
         *  Spring Session이 Redis를 세션 저장소로 사용하도록 설정되어 있으므로
         *  해당 세션 데이터는 Redis에 저장된다.
         */
        securityContextRepository.saveContext(
                context, request, response
        );

        /*
         *  STEP 6.
         *
         *  Authentication의 principal의
         *  로그인한 사용자 정보를 가져온다.
         */
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return LoginResponse.from(userDetails);
    }


    //  ======================================
    //  3. 관리자에 의한 강제 로그아웃
    //  ======================================
    @Transactional(readOnly = true)
    public void forceLogout(Long userId) {

        // 로그아웃시킬 사용자를 DB에서 조회한다.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new BusinessException(
                            ErrorCode.MEMBER_NOT_FOUND
                    )
                );

        /*
         *  Redis에 저장된 해당 사용자의 세션을 제거한다.
         *
         *  특정 서버의 메모리만 수정하는 것이 아니라
         *  여러 서버가 공유하는 세션 저장소에서 삭제한다.
         */
        userSessionManager.logoutAll(user.getEmail());
    }
}
