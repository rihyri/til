package io.github.rihyri.til.week05.day04.example.security;

import io.github.rihyri.til.week05.day04.example.user.User;
import io.github.rihyri.til.week05.day04.example.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. DB에서 이메일을 이용해 회원을 조회한다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

        // 2. 조회한 User는 JPA 엔티티이다.
        // Spring Security에서 인증에 사용하려면 UserDetails 형태로 변환해야 한다.
        return CustomUserDetails.from(user);
    }
}
