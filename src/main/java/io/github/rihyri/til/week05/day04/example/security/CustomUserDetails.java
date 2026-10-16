package io.github.rihyri.til.week05.day04.example.security;

import io.github.rihyri.til.week05.day04.example.user.User;
import io.github.rihyri.til.week05.day04.example.user.UserRole;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer, Serializable {

    private static final long serialVersionID = 1L;

    // DB에서 사용하는 사용자 식별자
    private final Long userId;

    // 로그인 ID
    private final String email;

    // USER 또는 ADMIN
    private final UserRole role;

    // 인증 과정에서만 필요한 비밀번호 해시
    // 인증이 끝난 후에는 null로 변경한다.
    private String password;

    public CustomUserDetails(Long userId, String email, UserRole role, String password) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    // DB에서 조회한 User 엔티티를 Spring Security가 사용하는 형태로 변환한다.
    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getPassword()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // Spring Security에서는 역할을 보통 ROLE_USER, ROLE_ADMIN 형태로 표현한다.
        // hasRole("ADMIN")은 내부적으로 ROLE_ADMIN 권한을 확인한다.
        SimpleGrantedAuthority authority
                = new SimpleGrantedAuthority("ROLE_" + role.name());

        return List.of(authority);
    }

    @Override
    public String getUsername() {
        // username 이름이지만 반드시 실제 이름일 필요는 없다.
        // 이번 프로젝트에서는 이메일을 사용한다.
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /*
     *  인증이 성공한 후에는 비밀번호 해시를 제거한다.
     *
     *  기존 ProviderManager는 인증 성공 시
     *  Authentication의 민감한 정보를 지우려고 한다.
     *
     *  Authentication 안의 Principal도 CredentialsContainer를 구현했다면
     *  해당 객체의 eraseCredentials()도 호출될 수 있다.
     */
    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
