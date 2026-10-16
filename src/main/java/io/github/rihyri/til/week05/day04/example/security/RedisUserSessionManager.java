package io.github.rihyri.til.week05.day04.example.security;

import lombok.RequiredArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUserSessionManager {

    /*
     *  일반 SessionRepository와 달리
     *  사용자 이름을 기준으로 세션을 검색할 수 있는 Indexed SessionRepository를 사용한다.
     *
     *  여기에서 사용자 이름은 로그인 ID인 이메일이다.
     */
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    // 특정 사용자에게 연결된 세션을 모두 제거한다.
    public void logoutAll(String email) {

        /*
         *  해당 이메일과 연결된 세션을 검색한다.
         *
         *  반환되는 Map :
         *  Key    = Session ID
         *  Value  = Session 정보
         */
        var sessions = sessionRepository.findByPrincipalName(email);

        /*
         *  세션 ID 별로 Redis 세션을 삭제한다.
         *
         *  삭제된 세션 ID로 다시 요청이 들어오면 기존 인증 정보를 복원할 수 없다.
         */
        sessions.keySet()
                .forEach(sessionRepository::deleteById);
    }
}
