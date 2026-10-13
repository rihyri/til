package io.github.rihyri.til.week05.day01.example.auth.service;

import io.github.rihyri.til.common.exception.BusinessException;
import io.github.rihyri.til.common.exception.ErrorCode;
import io.github.rihyri.til.week05.day01.example.auth.dto.LoginRequest;
import io.github.rihyri.til.week05.day01.example.auth.dto.LoginResponse;
import io.github.rihyri.til.week05.day01.example.auth.dto.SignUpRequest;
import io.github.rihyri.til.week05.day01.example.auth.entity.Member;
import io.github.rihyri.til.week05.day01.example.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp (SignUpRequest request) {

        // 1. 이미 가입한 이메일인지 확인
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 2. 비밀번호를 암호화한 후 회원 객체 생성
        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = new Member(
                request.name(),
                request.email(),
                encodedPassword
        );

        // 3. DB에 회원 저장
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public LoginResponse login (LoginRequest request) {

        // 1. 이메일을 기준으로 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_LOGIN)
                );

        // 2. 입력된 비밀번호와 DB의 암호회된 비밀번호 비교
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // 3. 로그인에 성공한 회원 정보 변환
        return toLoginResponse(member);
    }

    @Transactional(readOnly = true)
    public LoginResponse getLoginInfo (Long memberId) {

        // 세션에 저장된 회원 ID로 현재 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                );

        return toLoginResponse(member);
    }

    // Entity를 응답 DTO로 변환
    private LoginResponse toLoginResponse (Member member) {
        return new LoginResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
