package io.github.rihyri.til.week05.day02;

import io.github.rihyri.til.week05.day02.example.Week5Day02Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.*;

@SpringBootTest(classes = Week5Day02Application.class)
public class RedisDataTypeTest {

    @Autowired
    @Qualifier("day02RedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    private static final String STRING_KEY = "day02:visit:count";
    private static final String TTL_KEY = "day02:notice";
    private static final String LIST_KEY = "day02:recent:search";
    private static final String SET_KEY = "day02:event:users";
    private static final String SET_A_KEY = "day02:event:a";
    private static final String SET_B_KEY = "day02:event:b";
    private static final String HASH_KEY = "day02:member:10";
    private static final String ZSET_KEY = "day02:ranking";

    @BeforeEach
    void setUp() {
        clearTestData();
    }

    @AfterEach
    void tearDown() {
        clearTestData();
    }

    private void clearTestData() {
        // 각 테스트가 이전 테스트의 데이터에 영향을 받지 않도록 정리한다.
        redisTemplate.delete(List.of(
                STRING_KEY, TTL_KEY, LIST_KEY,
                SET_KEY, SET_A_KEY, SET_B_KEY,
                HASH_KEY, ZSET_KEY
        ));
    }

    @Test
    void stringTest() {

        // String은 단일 값이나 숫자 카운터를 저장할 때 사용할 수 있다.
        redisTemplate.opsForValue().set(STRING_KEY, "10");

        String savedCount = redisTemplate.opsForValue().get(STRING_KEY);

        assertThat(savedCount).isEqualTo("10");

        // 기존 숫자에 1을 더한다.
        Long updatedCount = redisTemplate.opsForValue().increment(STRING_KEY);

        assertThat(updatedCount).isEqualTo(11L);

        // 저장과 동시에 만료 시간을 지정한다.
        redisTemplate.opsForValue().set(TTL_KEY, "테스트 진행 중", Duration.ofMinutes(5));

        Long ttl = redisTemplate.getExpire(TTL_KEY);

        assertThat(ttl).isPositive();
    }

    @Test
    void listTest() {

        // List는 입력 순서를 유지하며 중복 데이터도 허용한다.
        redisTemplate.opsForList().rightPushAll(
                LIST_KEY,
                "Spring", "Redis", "Docker"
        );

        // 0부터 -1까지 조회하면 전체 요소를 가져온다.
        List<String> searchHistory =
                redisTemplate.opsForList().range(LIST_KEY, 0, -1);

        assertThat(searchHistory)
                .containsExactly("Spring", "Redis", "Docker");

        // 가장 왼쪽에 있는 데이터를 꺼내면서 제거한다.
        String firstSearch = redisTemplate.opsForList().leftPop(LIST_KEY);

        assertThat(firstSearch).isEqualTo("Spring");
    }

    @Test
    void setTest() {

        // Set은 중복을 허용하지 않고 순서를 보장하지 않는다.
        redisTemplate.opsForSet().add(
                SET_KEY,
                "memberA", "memberB", "memberA"
        );

        Set<String> members = redisTemplate.opsForSet().members(SET_KEY);

        assertThat(members).containsExactlyInAnyOrder("memberA", "memberB");

        // 두 이벤트에 참여한 회원을 비교한다.
        redisTemplate.opsForSet().add(
                SET_A_KEY, "memberA", "memberB"
        );

        redisTemplate.opsForSet().add(
                SET_B_KEY, "memberB", "memberC"
        );

        // 두 집합에 모두 포함된 회원
        Set<String> commonMembers =
                redisTemplate.opsForSet().intersect(SET_A_KEY, SET_B_KEY);

        // 두 집합에 포함된 모든 회원
        Set<String> allMembers = redisTemplate.opsForSet().union(SET_A_KEY, SET_B_KEY);

        // A 이벤트에만 참여한 회원
        Set<String> onlyA =
                redisTemplate.opsForSet().difference(SET_A_KEY, SET_B_KEY);

        assertThat(commonMembers).containsExactly("memberB");
        assertThat(allMembers)
                .containsExactlyInAnyOrder("memberA", "memberB", "memberC");
        assertThat(onlyA).containsExactly("memberA");
    }

    @Test
    void hashTest() {

        // Hash는 하나의 키 아래에 여러 필드와 값을 저장한다.
        redisTemplate.opsForHash().put(HASH_KEY, "nickname", "developer");
        redisTemplate.opsForHash().put(HASH_KEY, "level", "junior");
        redisTemplate.opsForHash().put(HASH_KEY, "region", "Seoul");

        Object nickname = redisTemplate.opsForHash().get(HASH_KEY, "nickname");
        assertThat(nickname).isEqualTo("developer");

        // 모든 필드를 Map 형태로 조회한다.
        Map<Object, Object> profile =
                redisTemplate.opsForHash().entries(HASH_KEY);

        assertThat(profile)
                .containsEntry("level", "junior")
                .containsEntry("region", "Seoul");

        // 특정 필드만 수정할 수도 있다.
        redisTemplate.opsForHash().put(HASH_KEY, "level", "middle");

        Object updatedLevel = redisTemplate.opsForHash().get(HASH_KEY, "level");

        assertThat(updatedLevel).isEqualTo("middle");
    }

    @Test
    void sortedSetTest() {

        // Sorted Set은 중복되지 않는 멤버와 점수를 함께 저장한다.
        redisTemplate.opsForZSet().add(ZSET_KEY, "teamA", 75);
        redisTemplate.opsForZSet().add(ZSET_KEY, "teamB", 95);
        redisTemplate.opsForZSet().add(ZSET_KEY, "teamC", 85);

        // 점수가 높은 순서대로 상위 두 팀을 조회한다.
        Set<String> topTeams =
                redisTemplate.opsForZSet().reverseRange(ZSET_KEY, 0, 1);

        assertThat(topTeams).containsExactly("teamB", "teamC");

        // 기존 점수에 15점을 추가한다.
        Double updatedStore =
                redisTemplate.opsForZSet()
                        .incrementScore(ZSET_KEY, "teamA", 15);

        assertThat(updatedStore).isEqualTo(90.0);

        // 내림차순 순위는 0부터 시작한다.
        Long rank = redisTemplate.opsForZSet()
                .reverseRank(ZSET_KEY, "teamA");

        assertThat(rank).isEqualTo(1L);
    }
}
