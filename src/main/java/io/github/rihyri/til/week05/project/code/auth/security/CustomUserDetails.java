package io.github.rihyri.til.week05.project.code.auth.security;

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

    private final Long userId;

    private final String email;

    private final UserRole role;

    private String password;

    public CustomUserDetails(Long userId, String email, UserRole role, String password) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    // DB에서 조회한 User 엔티티를 Spring Security가 사용하는 형태로 변환한다.
    public static io.github.rihyri.til.week05.day04.example.security.CustomUserDetails from(User user) {
        return new io.github.rihyri.til.week05.day04.example.security.CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getPassword()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        SimpleGrantedAuthority authority
                = new SimpleGrantedAuthority("ROLE_" + role.name());

        return List.of(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

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
