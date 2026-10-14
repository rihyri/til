<br>

# Week 05 - Day 02 : 인메모리 저장소와 Redis

<br>

## 1. 인메모리 저장소 (In-Memory Storage)

<br>

인메모리 저장소는 데이터를 디스크가 아닌 메모리(RAM)에 보관하여 빠르게 읽고 쓰는 기술이다.

일반적인 디스크 기반 저장소보다 데이터 접근 속도가 빠르기 때문에 응답 시간을 줄여야 하는 작업에서 많이 활용된다.

#### 대표적인 활용 사례
- 캐싱 : 자주 조회하는 데이터를 임시 보관
- 세션 관리 : 사용자별 로그인 상태나 임시 정보 유지
- 실시간 처리 : 조회수, 랭킹, 접속 상태 관리

<br>

### Redis와 Memcached 비교

<table>
    <thead>
        <th>구분</th>
        <th>Redis</th>
        <th>Memcached</th>
    </thead>
    <tbody>
        <tr>
            <td>데이터 구조</td>
            <td>다양한 자료구조 지원</td>
            <td>단순한 Key-Value</td>
        </tr>
        <tr>
            <td>영속성</td>
            <td>RDB, AOF 지원</td>
            <td>기본적으로 미지원</td>
        </tr>
        <tr>
            <td>주요 용도</td>
            <td>캐시, 세션, 실시간 랭킹</td>
            <td>단순 데이터 캐싱</td>
        </tr>
        <tr>
            <td>특징</td>
            <td>풍부한 데이터 처리 기능</td>
            <td>단순하고 가벼운 구조</td>
        </tr>
    </tbody>
</table>

<br>

Redis는 단순한 캐시 외에도 다양한 형태의 데이터를 관리할 수 있다는 장점이 있다.


<br>
<hr>

## 2. Redis란?

<br>

Redis는 메모리를 중심으로 데이터를 처리하는 오픈소스 데이터 저장소로, `Key-Value` 기반의 여러 자료구조를 제공한다.

<br>

### 주요 특징
1. **빠른 데이터 접근** : 주로 메모리에서 데이터를 처리한다.
2. **다양한 자료구조** : String, List, Set, Hash, Sorted Set 등을 지원한다.
3. **TTL 지원** : 일정 시간이 지나면 데이터를 자동 삭제할 수 있다.
4. **영속성 옵션** : RDB와 AOF를 사용해 데이터를 디스크에 저장할 수 있다.

<br>

### TTL (Time To Live)
TTL은 데이터가 유효한 시간을 의미한다.

```aiignore
SET notice "hello" EX 60
TTL notice
```

위 명령어는 notice라는 키를 저장하고 60초 뒤에 만료되도록 설정한다.

`TTL`은 인증번호, 임시 세션 데이터, 캐시와 같이 일정 기간만 필요한 정보를 관리할 때 유용하다.

<br>

### Redis의 영속성

- **RDB** : 특정 시점의 데이터 상태를 스냅샷으로 저장한다.
- **AOF** : 쓰기 작업 내역을 기록하여 데이터 복구에 활용한다.

두 방식을 함께 사용하거나 영속성을 비활성화할 수도 있다.

Redis가 영속성을 지원한다고 해서 모든 데이터가 자동으로 완벽하게 보존되는 것은 아니다. 실제 복구 가능 범위는 설정에 따라 달라진다.

<br>
<hr>

## 3. Spring Boot와 Redis 연동

<br>

Spring Boot에서는 `Spring Data Redis`를 이용해 Redis를 쉽게 사용할 수 있다.

<br>

#### 주요 의존성

- spring-boot-starter-data-redis : Redis 연결 및 데이터 조작
- spring-session-data-redis : HTTP 세션을 Redis에 저장

<br>

### RedisTemplate

`RedisTemplate`은 Java 애플리케이션에서 Redis 데이터에 접근하는 기능을 제공한다.

```java
redisTemplate.opsForValue().set("language", "Java");

String language =
        redisTemplate.opsForValue().get("language");
```

`Redis CLI` 명령어를 직접 사용하지 않고도 다양한 자료구조를 사용할 수 있다.

이때 중요한 개념이 `직렬화 (Serialization)`이다.

직렬화란 Java 객체를 Redis에 저장할 수 있는 데이터 형태로 변환하는 과정이며, 역직렬화는 저장된 데이터를 다시 Java 객체로 복원하는 과정이다.

문자열 기반 데이터라면 `StringRedisSerializer`를 사용해 관리할 수 있다.

<br>
<hr>

## 4. Spring Session과 Redis

<br>

### HTTP 세션이란?

HTTP는 기본적으로 이전 요청의 상태를 기억하지 않는다.

따라서 사용자별 상태를 유지하려면 세션과 같은 별도 관리 방법이 필요하다.

Spring MVC에서는 `HttpSession`을 이용해 사용자별 정보를 저장하고 조회할 수 있다.

```java
// 세션에 값 저장
session.setAttribute("theme", "dark");

// 세션에서 값 조회
String theme =
        (String) session.getAttribute("theme");

// 세션 무효화
session.invalidate();
```

<br>

### Spring Session의 역할

기본적인 서버 세션은 애플리케이션 서버에서 관리된다.

하지만 Spring Session과 Redis를 연동하면 세션 저장소를 외부 Redis로 분리할 수 있다.

#### · 처리 과정
1. 클라이언트가 서버에 요청을 보낸다.
2. Spring Session이 요청의 세션 정보를 확인한다.
3. 세션에 저장된 데이터가 Redis에 기록된다.
4. 서버는 세션 식별자를 쿠키로 전달한다.
5. 이후 요청에서 같은 쿠키를 이용해 세션 데이터를 찾는다.

이 방식을 활용하면 여러 애플리케이션 서버가 동일한 Redis 세션 저장소를 공유할 수 있다.

<br>

### Redis CLI로 확인하기

```aiignore
SCAN 0 MATCH spring:session:sessions:* COUNT 100
```

세션 키를 확인한 후 다음 명령어를 사용한다.

```aiignore
HGETALL spring:session:sessions:<세션ID>
```

Spring Session의 구성에 따라 생성 시간, 마지막 접근 시간, 만료 기간, 세션 속성 등을 확인할 수 있다.

**주의 :** Redis에 저장된 HTTP세션과 RedisTemplate으로 직접 저장하는 일반 데이터는 서로 다른 방식으로 관리될 수 있다.

<br>
<hr>

## 5. Redis의 주요 자료구조

<br>

Redis는 저장하려는 데이터의 특징에 따라 적절한 자료구조를 선택할 수 있다.

<br>

<table>
    <thead>
        <th>자료구조</th>
        <th>특징</th>
        <th>활용 사례</th>
    </thead>
    <tbody>
        <tr>
            <td>String</td>
            <td>하나의 값 저장</td>
            <td>조회수, 인증번호</td>
        </tr>
        <tr>
            <td>List</td>
            <td>순서 유지, 중복 허용</td>
            <td>작업 대기열, 최근 기록</td>
        </tr>
        <tr>
            <td>Set</td>
            <td>중복 제거, 순서 미보장</td>
            <td>이벤트 참여자</td>
        </tr>
        <tr>
            <td>Hash</td>
            <td>필드와 값의 묶음</td>
            <td>사용자 프로필</td>
        </tr>
        <tr>
            <td>Sorted Set</td>
            <td>점수를 기준으로 정렬</td>
            <td>순위표, 리더보드</td>
        </tr>
    </tbody>
</table>

<br>

### 5-1. String

가장 기본적인 자료구조로 문자열과 숫자 등을 저장한다.

```java
redisTemplate.opsForValue().set("views", "10");
redisTemplate.opsForValue().increment("views");
```

숫자로 표현할 수 있는 문자열은 `increment()`등을 이용해 값을 증가시킬 수 있다.

<br>

### 5-2. List

데이터의 순서를 유지하며 동일한 값을 여러 번 저장할 수 있다.

```java
redisTemplate.opsForList().rightPush("tasks", "taskA");
redisTemplate.opsForList().rightPush("tasks", "taskB");

redisTemplate.opsForList().leftPop("tasks");
```

오른쪽에 추가하고 왼쪽에서 꺼내면 FIFO (First in, First Out) 방식으로 사용할 수 있다.

<br>

### 5-3. Set

중복을 허용하지 않는 집합 구조이다.

```java
redisTemplate.opsForSet().add("members", "userA", "userB");
```

또한 교집합, 합집합, 차집합 연산을 지원한다.

- `intersect()` : 두 집합에 공통으로 포함된 값
- `union()` : 두 집합의 모든 고유 값
- `difference()` : 첫 번째 집합에만 포함된 값

<br>

### 5-4. Hash

하나의 Redis 키 안에 여러 필드와 값을 저장하는 자료구조이다.

```java
redisTemplate.opsForHash().put("member:1", "nickname", "dev");
redisTemplate.opsForHash().put("member:1", "level", "junior");
```

사용자 프로필이나 상품 속성과 같이 여러 항목으로 이루어진 데이터를 관리할 때 활용할 수 있다.

<br>

### 5-5. Sorted Set

Set처럼 멤버의 중복을 허용하지 않지만, 각 멤버에 점수(Score)를 부여한다.

```java
redisTemplate.opsForZSet().add("ranking", "userA", 90);
redisTemplate.opsForZSet().add("ranking", "userB", 75);
```

점수를 기준으로 데이터를 정렬하여 조회할 수 있으므로 실시간 랭킹 기능에 적합하다.

`rank()`는 오름차순, `reverseRank()`는 내림차순 순위를 반환하며, 두 메서드 모두 0부터 시작한다.

<br>
<hr>

## 6. Redis 사용 시 고려할 점

<br>

### Key 네이밍

Redis는 일반적으로 콜론(:)으로 의미를 구분하여 Key를 작성한다.

```aiignore
member:10:profile
article:25:views
event:1:participants
```

이런 규칙을 사용하면 데이터의 목적을 쉽게 파악할 수 있다.

<br>

### 데이터 만료와 관리

Redis는 메모리 자원을 사용하므로 불필요한 데이터가 계속 쌓이지 않도록 관리하는 것이 중요하다.

- 임시 데이터에는 적절한 TTL을 설정한다.
- 영구 보관이 필요한 데이터는 별도 저장 전략을 고려한다.
- 큰 데이터 구조와 과도한 키 생성을 주의한다.
- 운영 환경에서는 `KEYS *` 보다 `SCAN`을 이용한 점진적 탐색이 적합하다.


<br>
<hr>

## 7. 핵심 정리

<br>

- Redis는 빠른 접근 속도와 다양한 자료구조가 특징인 인메모리 데이터 저장소이다.
- Spring Data Redis의 `RedisTemplate`으로 Redis 데이터를 직접 조작할 수 있다.
- Spring Session을 이용하면 HTTP 세션을 Redis에 저장하고 여러 서버에서 공유할 수 있다.
- Redis 자료 구조는 데이터 특성에 맞게 선택해야 한다.
- TTL과 영속성은 데이터의 수명과 복구 가능성을 관리하는 중요한 기능이다.

<br>
