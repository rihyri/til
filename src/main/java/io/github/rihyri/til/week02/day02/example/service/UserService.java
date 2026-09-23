package io.github.rihyri.til.week02.day02.example.service;

import io.github.rihyri.til.week02.day02.example.dto.UserCreateRequest;
import io.github.rihyri.til.week02.day02.example.dto.UserResponse;
import io.github.rihyri.til.week02.day02.example.entity.User;
import io.github.rihyri.til.week02.day02.example.mapper.UserMapper;
import io.github.rihyri.til.week02.day02.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {

        // 1. 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 3. Builder를 통해 Entity 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .build();
        
        // 4. DB 저장
        User savedUser = userRepository.save(user);
        
        // 5. Entity → Response DTO 변환
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return userMapper.toResponse(user);
    }
}
