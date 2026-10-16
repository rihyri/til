<br>

# Week 5. Spring Boot - Session, Redis & Spring Security

<br>

5주차에는 HTTP의 Stateless 특성과 Session을 이용한 상태 관리부터, Redis를 활용한 데이터 저장 및 캐싱, Spring Security 기반 인증까지 학습했다.

4주차가 **LLM에 필요한 데이터를 제공하고 AI 기능을 확장하는 과정**이었다면, 5주차는 **서버에서 사용자 상태를 관리하고, 데이터 접근 성능을 개선하며, 안전한 인증 시스템을 구축하는 방법**에 집중했다. 

전체적인 흐름은 다음과 같다.

```aiignore
HTTP 요청 → Session을 통한 상태 관리 → Redis를 활용한 데이터 저장 및 캐싱 

→ Spring Security 인증 및 인가
```

<br>
<hr>

## 1. HTTP Session과 상태 관리

<br>

### · HTTP와 Stateless

HTTP는 기본적으로 Stateless(무상태) 프로토콜이다.

서버는 각각의 요청을 독립적으로 처리하며, 이전 요청에서 사용자가 어떤 작업을 수행했는지 자동으로 기억하지 않는다.

그러나 로그인 상태처럼 여러 요청에 걸쳐 유지해야 하는 정보가 존재한다. 이를 위해 Cookie와 Session을 활용할 수 있다.

<br>

### · Cookie와 Session

<table>
    <thead>
        <th>구분</th>
        <th>Cookie</th>
        <th>Session</th>
    </thead>
    <tbody>
        <tr>
            <td>관리 위치</td>
            <td>클라이언트</td>
            <td>일반적으로 서버</td>
        </tr>
        <tr>
            <td>주요 역할</td>
            <td>데이터 및 식별자 전달</td>
            <td>사용자 상태 관리</td>
        </tr>
        <tr>
            <td>저장 정보</td>
            <td>설정값, 세션 ID 등</td>
            <td>로그인 정보, 사용자 데이터</td>
        </tr>
        <tr>
            <td>유효기간</td>
            <td>쿠키 만료 정책</td>
            <td>세션 만료 정책</td>
        </tr>
    </tbody>
</table>

<br>

일반적인 세션 기반 로그인에서는 서버가 사용자 상태를 보관하고, 클라이언트는 세션 ID를 Cookie에 저장하여 전달한다.


#### 세션 인증 흐름

```aiignore
1. 사용자 로그인 요청 → 2. 서버에서 사용자 인증 → 3. 인증 성공 시 Session 생성 

→ 4. Session ID를 Cookie로 전달 → 5. 이후 요청에서 Session ID 전송 

→ 6. 서버에서 Session 조회 후 사용자 식별
```

Spring MVC에서는 `HttpSession`을 통해 세션 데이터를 관리할 수 있다.

- `setAttribute()` : 데이터 저장
- `getAttribute()` : 데이터 조회
- `removeAttribute()` : 특정 데이터 제거
- `invalidate()` : 세션 무효화

<br>

### · Session Clustering

서버가 여러 대인 환경에서는 각 서버의 메모리에 세션을 저장할 경우 사용자 상태가 공유되지 않는 문제가 발생할 수 있다.

이를 해결하기 위한 대표 방식은 다음과 같다.

<table>
    <thead>
        <th>방식</th>
        <th>특징</th>
    </thead>
    <tbody>
        <tr>
            <td>Sticky Session</td>
            <td>사용자를 동일한 서버로 전달</td>
        </tr>
        <tr>
            <td>Session Replication</td>
            <td>서버 간 세션 데이터 복제</td>
        </tr>
        <tr>
            <td>Centralized Session Store</td>
            <td>Redis 등 공통 저장소에서 세션 관리</td>
        </tr>
    </tbody>
</table>

이번 학습에서는 **Redis를 중앙 세션 저장소로 사용하는 방식**을 중심으로 다루었다.

여러 서버가 동일한 Redis를 사용하면 요청을 처리하는 서버가 달라져도 기존 로그인 상태를 유지할 수 있다.

<br>
<hr>

## 2. Redis와 인메모리 데이터 저장소

<br>

### · Redis란?

Redis는 메모리(RAM)을 중심으로 데이터를 처리하는 Key-Value 기반 저장소이다.

디스크 기반 저장소보다 빠르게 데이터에 접근할 수 있어 캐싱, 세션 관리, 실시간 데이터 처리 등에 활용된다.

<br>

#### 주요 특징

- **In-Memory** : 메모리 기반의 빠른 데이터 처리
- **Key-Value** : Key를 이용한 데이터 접근
- **TTL** : 데이터의 만료 시간 설정
- **다양한 자료구조** : 데이터 특성에 맞는 저장 방식 제공
- **Persistence** : RDB와 AOF를 이용한 데이터 복구 지원

<br>

### · Redis의 주요 자료구조

<table>
    <thead>
        <th>자료구조</th>
        <th>특징</th>
        <th>활용 사례</th>
    </thead>
    <tbody>
        <tr>
            <td>String</td>
            <td>단일 값 저장</td>
            <td>인증번호, 조회수</td>
        </tr>
        <tr>
            <td>List</td>
            <td>순서 유지, 중복 허용</td>
            <td>작업 대기열</td>
        </tr>
        <tr>
            <td>Set</td>
            <td>중복 없는 집합</td>
            <td>이벤트 참여자</td>
        </tr>
        <tr>
            <td>Hash</td>
            <td>Field-Value 구조</td>
            <td>사용자 정보</td>
        </tr>
        <tr>
            <td>Sorted Set</td>
            <td>Score 기반 정렬</td>
            <td>실시간 랭킹</td>
        </tr>
    </tbody>
</table>

저장하려는 데이터의 형태와 처리 방식에 맞는 자료구조를 선택하는 것이 중요하다.

<br>

### · Spring Boot와 Redis

Spring에서는 `Spring Data Redis`를 통해 Redis에 접근할 수 있다.

대표적인 구성 요소는 다음과 같다.

- `RedisTemplate` : Redis 데이터를 직접 저장, 조회, 삭제
- `StringRedisTemplate` : 문자열 기반 데이터 처리
- `Spring Session` : HTTP 세션을 외부 저장소에서 관리

Java 객체를 Redis에 저장할 때는 직렬화(Serialization)가 필요하며, 저장된 데이터를 다시 Java 객체로 복원하는 과정을 역직렬화(DeSerialization)라고 한다.

<br>
<hr>

## 3. Redis 캐싱 전략

<br>

### · Cache 란?

캐싱은 자주 사용하는 데이터를 빠른 저장소에 보관하여 반복적인 조회 비용을 줄이는 기술이다.

Redis를 캐시로 활용하면 DB 접근 횟수를 줄이고 응답 속도를 개선할 수 있다.

```aiignore
사용자 조회 요청
       ↓
    Redis 조회
       ↓
  Cache Hit?
   ├─ Yes → 캐시 데이터 반환
   └─ No  → DB 조회 → Redis 저장 → 반환
```

단, 캐시와 원본 데이터 사이의 일관성을 유지하기 위한 전략이 필요하다.

<br>

### · 주요 캐싱 전략

<table>
    <thead>
        <th>전략</th>
        <th>동작 방식</th>
        <th>주요 목적</th>
    </thead>
    <tbody>
        <tr>
            <td>Cache-aside</td>
            <td>캐시 조회 후 없으면 DB 조회</td>
            <td>읽기 성능 개선</td>
        </tr>
        <tr>
            <td>Write-through</td>
            <td>DB 변경과 함께 캐시 갱신</td>
            <td>최신 데이터 유지</td>
        </tr>
        <tr>
            <td>Write-back</td>
            <td>캐시에 먼저 기록 후 DB 반영</td>
            <td>쓰기 성능 개선</td>
        </tr>
        <tr>
            <td>Write-around</td>
            <td>DB 변경 후 캐시 우회 또는 무효화</td>
            <td>불필요한 캐시 갱신 방지</td>
        </tr>
    </tbody>
</table>

**Cache-aside**는 조회 요청이 많고 데이터 변경이 적은 환경에서 유용하다.

반면 **Write-back**은 쓰기 성능에 유리하지만, DB 반영 전 장애가 발생하면 데이터가 유실될 위험이 있어 재시도와 복구 전략이 필요하다.

<br>

### · TTL과 Eviction

Redis는 메모리 기반 저장소이므로 데이터의 수명과 메모리 사용량을 관리해야 한다.

- **TTL (Time To Live)** : 지정된 시간이 지나면 데이터를 만료시키는 기능
- **Eviction** : 메모리 제한에 도달했을 때 설정된 정책에 따라 데이터를 제거하는 기능

대표적인 Eviction 정책은 다음과 같다.

- `LRU` : 최근 사용되지 않은 데이터를 우선 제거
- `LFU` : 사용 빈도가 낮은 데이터를 우선 제거
- `noeviction` : 메모리가 부족하면 새로운 데이터 쓰기 거부

TTL은 데이터의 유효기간을 관리하고, Eviction은 메모리 공간을 관리한다는 차이가 있다.

<br>

### · Redis 영속성

Redis는 메모리 데이터를 디스크에 기록하기 위한 영속성 기능을 제공한다.

<table>
    <thead>
        <th>구분</th>
        <th>RDB</th>
        <th>AOF</th>
    </thead>
    <tbody>
        <tr>
            <td>저장 방식</td>
            <td>특정 시점의 Snapshot</td>
            <td>쓰기 명령 기록</td>
        </tr>
        <tr>
            <td>장점</td>
            <td>백업 및 대규모 복구에 유리</td>
            <td>최근 변경 사항 복구에 유리</td>
        </tr>
        <tr>
            <td>단점</td>
            <td>Snapshot 사이 데이터 유실 가능</td>
            <td>로그 기록에 따른 I/O 비용</td>
        </tr>
    </tbody>
</table>

Redis가 영속성을 지원하더라도 설정과 장애 상황에 따라 데이터가 유실될 수 있다.

따라서 **캐시처럼 재생성 가능한 데이터인지, 반드시 보존해야 하는 데이터인지**에 따라 영속성 정책을 결정해야 한다. 

<br>
<hr>

## 4. Spring Security와 인증 구조

<br>

### · Authentication과 Authorization

Spring Security는 Spring 애플리케이션에서 인증과 인가 기능을 제공하는 보안 프레임워크이다.

- **Authentication (인증)** : 사용자가 누구인지 확인하는 과정
- **Authorization (인가)** : 인증된 사용자가 특정 기능에 접근할 권한이 있는지 확인하는 과정

Spring Security는 Servlet Filter 기반으로 동작하므로 Controller에 요청이 전달되기 전에 인증 및 접근 제어를 수행할 수 있다.

<br>

### · Spring Security 주요 구성 요소

<table>
    <thead>
        <th>구성 요소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>SecurityFilterChain</td>
            <td>보안 필터 및 접근 규칙 구성</td>
        </tr>
        <tr>
            <td>AuthenticationManager</td>
            <td>인증 처리 위임</td>
        </tr>
        <tr>
            <td>AuthenticationProvider</td>
            <td>실제 인증 수행</td>
        </tr>
        <tr>
            <td>UserDetailsService</td>
            <td>사용자 정보 조회</td>
        </tr>
        <tr>
            <td>UserDetails</td>
            <td>인증에 필요한 사용자 정보 제공</td>
        </tr>
        <tr>
            <td>PasswordEncoder</td>
            <td>비밀번호 해시 및 검증</td>
        </tr>
        <tr>
            <td>SecurityContext</td>
            <td>인증된 사용자 정보 보관</td>
        </tr>
    </tbody>
</table>

<br>

### · 로그인 인증 과정

일반적인 `DaoAuthenticationProvider` 기반 인증 흐름은 다음과 같다.

```aiignore
로그인 요청
    ↓
AuthenticationManager.authenticate()
    ↓
DaoAuthenticationProvider
    ├─ UsersDetailService → 회원 조회
    └─ PasswordEncoder → 비밀번호 검증
    ↓
인증 성공
    ↓
Authentication 반환
    ↓
SecurityContext에 인증 정보 저장
```

`AuthenticationManager`는 인증을 처리할 Provider에 작업을 위임하며,
`DaoAuthenticationProvider`는 사용자 조회와 비밀번호 검증을 수행한다.

인증에 성공하면 `Authentication` 객체를 통해 인증된 사용자 정보와 권한 등을 관리할 수 있다.

<br>

### · SecurityContext와 세션

`SecurityContext`는 현재 인증된 사용자의 `Authentication` 정보를 보관한다.

- `SecurityContextHolder` : 현재 실행 흐름의 SecurityContext 관리
- `SecurityContextRepository` : 요청 사이에 SecurityContext를 저장하고 조회

직접 로그인 로직을 구현하는 경우에는 인증 성공 후 SecurityContext를 설정하고, 다음 요청에서도 인증 상태가 유지되도록 저장해야 한다.

<br>
<hr>

## 5. Spring Security와 Redis Session 연동

<br>

Spring Security는 사용자 인증을 담당하고, Spring Session은 HTTP 세션의 저장 및 조회를 담당한다.

Redis를 세션 저장소로 연결하면 인증된 사용자의 상태를 여러 서버에서 공유할 수 있다.

<br>

### · Redis 기반 세션 인증 흐름

```aiignore
[사용자 로그인] → Spring Security 인증 → Authentication 생성 → SecurityContext 저장

→ Spring Session → Redis에 세션 데이터 저장 → 클라이언트에 Session ID 전달
```

이후 사용자는 SESSION ID를 전달하고, 서버는 Redis에 저장된 세션을 통해 인증 상태를 복원할 수 있다. 

<br>

### · 세션 관리 전략

로그인에 성공한 사용자의 세션을 안전하게 관리하기 위해 `SessionAuthenticationStrategy`를 활용할 수 있다.

대표적인 구현체는 다음과 같다. 

- `ConcurrentSessionControlAuthenticationStrategy` : 동시 로그인 개수 제한
- `ChangeSessionIdAuthenticationStrategy` : 로그인 후 세션 ID 변경
- `RegisterSessionAuthenticationStrategy` : 인증된 세션 등록

또한 `SessionRegistry`를 통해 사용자별 활성 세션을 조회하고, 정책에 따라 특정 세션을 만료 처리할 수 있다.

이를 활용하면 중복 로그인 제한, 관리자 강제 로그아웃 등의 기능을 구현할 수 있다.

<br>
<hr>

## 5주차 핵심 흐름

<br>

이번 주에 학습한 기술들을 하나의 백엔드 서비스에 연결하면 다음과 같다.

<br>

### 1. 사용자 상태 관리

```aiignore
HTTP 요청 → Cookie의 Session ID 확인 → HttpSession 조회 → 사용자 상태 확인
```

### 2. Redis 기반 데이터 처리
```aiignore
데이터 조회 요청 → Redis 캐시 조회 → Cache Hit / Miss 판단 → 필요 시 DB 조회
```

### 3. 인증 및 세션 공유
```aiignore
로그인 요청 → Spring Security 인증 → SecurityContext 생성 및 저장

→ Spring Session → Redis 세션 저장
```

이러한 구조를 통해 여러 서버가 동일한 사용자 인증 상태를 공유하고, Redis 캐싱을 이용해 반복적인 DB 조회 비용을 줄일 수 있다.

<br>
<hr>

## 마치며

<br>

5주차에는 HTTP의 `Stateless` 특성으로 인해 발생하는 상태 관리 문제를 이해하고, Session을 이용해 사용자 정보를 유지하는 방법을 학습했다.

이어서 Redis의 자료구조와 캐싱 전략을 통해 데이터 접근 성능을 개선하는 방법을 살펴보고, Spring Security를 이용해 인증과 인가를 처리하는 구조를 학습했다.

이번 주의 핵심은 **세션, Redis, Spring Security가 각각 어떤 역할을 담당하며 하나의 백엔드 서비스에서 어떻게 연결되는지** 이해하는 것이었다.

```aiignore
Session → 사용자 상태 유지
Redis → 빠른 데이터 접근 및 세션 공유
Caching → DB 부하 감소와 성능 개선
Spring Security → 인증 및 접근 권한 관리 
```

결국 5주차의 학습은 **사용자 상태를 안전하게 유지하면서, 여러 서버 환경에서도
일관성과 성능을 확보하려면 어떻게 설계해야 하는가?**라는 질문으로 정리할 수 있다.

<br>

