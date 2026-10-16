<br>

# Week05 - Day 04. Spring Security와 Redis 기반 세션 인증

<br>

## 1. Spring Security

<br>

Spring Security는 Spring 애플리케이션에서 인증과 인가 기능을 제공하는 보안 프레임워크다.

- **인증 (Authentication)** : 사용자가 누구인지 확인하는 과정
- **인가 (Authorization)** : 인증된 사용자가 특정 기능에 접근할 권한이 있는지 확인하는 과정

예를 들어 로그인은 인증 과정이며, 일반 사용자의 관리자 API 접근을 제한하는 것은 인가 과정이다.

Spring Security는 `Servlet Filter` 기반으로 동작하므로 Controller에 요청이 전달되기 전에 보안 관련 처리를 수행할 수 있다.

<br>
<hr>

## 2. Spring Security 인증 구조

<br>

### 2-1. 주요 구성 요소

<table>
    <thead>
        <th>구성요소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>SecurityFilterChain</td>
            <td>요청에 적용할 보안 필터 구성</td>
        </tr>
        <tr>
            <td>Authentication</td>
            <td>인증 요청 또는 인증 결과 정보</td>
        </tr>
        <tr>
            <td>AuthenticationManager</td>
            <td>인증 처리를 요청하는 인터페이스</td>
        </tr>
        <tr>
            <td>ProviderManager</td>
            <td>AuthenticationManager의 대표적인 구현체</td>
        </tr>
        <tr>
            <td>AuthenticationProvider</td>
            <td>실제 인증 로직을 수행하는 구성 요소</td>
        </tr>
        <tr>
            <td>UserDetailsService</td>
            <td>사용자 정보를 조회</td>
        </tr>
        <tr>
            <td>UserDetails</td>
            <td>Spring Security에서 사용하는 사용자 정보</td>
        </tr>
        <tr>
            <td>PasswordEncoder</td>
            <td>비밀번호 해시 및 검증</td>
        </tr>
        <tr>
            <td>SecurityContext</td>
            <td>인증된 사용자 정보 보관</td>
        </tr>
        <tr>
            <td>SecurityContextHolder</td>
            <td>현재 실행 흐름의 SecurityContext 관리</td>
        </tr>
    </tbody>
</table>

<br>

### 2-2. 로그인 인증 과정
1. 클라이언트가 이메일과 비밀번호를 전달한다.
2. `UsernamePasswordAuthenticationToken`으로 인증 요청 객체를 생성한다.
3. `AuthenticationManager`에 인증을 요청한다.
4. `DaoAuthenticationProvider`가 `UserDetailsService`를 통해 사용자를 조회한다.
5. `PasswordEncoder`가 입력한 비밀번호와 저장된 비밀번호 해시를 비교한다.
6. 인증에 성공하면 인증된 `Authentication` 객체를 반환한다.
7. 반환된 인증 정보를 `SecurityContext`에 저장한다. 

`AuthenticationManager`는 인증의 진입점이고, `AuthenticationProvider`는 실제 인증 방식을 구현한다는 차이가 있다.

<br>
<hr>

## 3. UserDetails와 사용자 정보 관리

<br>

### 3-1. User와 UserDetails의 차이

- `User` : 애플리케이션에서 사용하는 회원 엔티티
- `UserDetails` : Spring Security가 인증에 활용할 사용자 정보를 표현하는 인터페이스
- `UserDetailsService` : 저장소에서 회원을 조회하고 UserDetails 형태로 제공하는 인터페이스

일반적으로 `UserDetailsService`의 `loadUserByUsername()`을 구현하여 DB에서 회원을 조회한다.

<br>

### 3-2. 주요 인터페이스

#### · UserDetails
사용자의 식별자, 비밀번호, 권한, 계정 상태 등을 제공하기 위한 규칙이다.

#### · CredentialsContainer
`eraseCredentials()`를 통해 인증 이후 불필요한 비밀번호 등의 민감한 정보를 제거할 수 있다.

기본 `ProviderManager`는 인증 성공 후 CredentialsContainer를 활용하여 민감한 정보 제거를 시도한다.

#### · Serializable

객체를 바이트 형태로 직렬화할 수 있도록 하는 인터페이스이다.

Spring Session에서 JDK 직렬화를 사용하는 경우 세션에 저장되는 사용자 객체도 직렬화 가능해야 한다.

<br>
<hr>

## 4. Spring Session과 Redis

<br>

### 4-1. 세션을 Redis에 저장하는 이유

일반적인 서버 메모리 기반 세션에서는 서버마다 서로 다른 세션 저장소를 사용한다.

서버 A에서 로그인한 사용자의 다음 요청이 서버 B로 전달되면 서버 B에서 해당 세션을 찾지 못할 수 있다.

Spring Session과 Redis를 함께 사용하면 여러 애플리케이션 서버가 동일한 세션 저장소를 공유할 수 있다.

주요 장점은 다음과 같다.

- 서버 간 로그인 세션 공유
- 세션 만료 시간 관리
- 사용자별 세션 조회
- 관리자에 의한 강제 로그아웃
- 다중 서버 환경에서 세션 관리 일관성 향상

<br>

#### 4-2. 세션 직렬화

Redis에 Java 객체를 저장하려면 데이터 변환이 필요하다.

- 직렬화 : 객체를 저장 가능한 데이터 형태로 변환
- 역직렬화 : 저장된 데이터를 다시 객체로 복원

JDK 직렬화는 Java 객체를 바이트 데이터로 변환하며, 관련 객체는 Serializable 조건을 만족해야 한다.

DevTools 환경에서는 RestartClassLoader와 일반 ClassLoader의 차이로 인해 역직렬화 문제가 발생할 수 있다. 적절한 ClassLoader를 지정하면 이러한 문제를 예방하는 데 도움이 된다.

<br>
<hr>

## 5. SecurityContext와 세션 저장

<br>

`SecurityContext`는 현재 인증된 사용자의 Authentication 정보를 보관한다.

`SecurityContextHolder`는 현재 실행 흐름에서 SecurityContext에 접근할 수 있도록 한다.

`SecurityContextRepository`는 SecurityContext를 요청 사이에 유지하기 위한 저장 및 조회 기능을 제공한다.

직접 구현한 로그인에서는 다음 과정이 필요하다.

1. 인증된 Authentication 객체 획득
2. SecurityContext 생성
3. Authentication 저장
4. SecurityContextHolder에 설정
5. SecurityContextRepository를 통해 세션에 저장

Spring Session이 적용된 환경에서는 HttpSession의 실제 저장소로 Redis를 사용할 수 있다.

<br>
<hr>

## 6. 세션 인증 전략

<br>

로그인 성공 후 세션을 어떻게 처리할지 결정하는 구성 요소가 `SessionAuthenticationStrategy`이다.

<br>

## 6-1. 주요 구현체

<table>
    <thead>
        <th>구현체</th>
        <th>기능</th>
    </thead>
    <tbody>
        <tr>
            <td>ConcurrentSessionControlAuthenticationStrategy</td>
            <td>동시 로그인 개수 제한</td>
        </tr>
        <tr>
            <td>ChangeSessionIdAuthenticationStrategy</td>
            <td>세션 ID 변경</td>
        </tr>
        <tr>
            <td>RegisterSessionAuthenticationStrategy</td>
            <td>인증된 세션 등록</td>
        </tr>
        <tr>
            <td>CompositeSessionAuthenticationStrategy</td>
            <td>여러 세션 전략을 순서대로 실행</td>
        </tr>
    </tbody>
</table>

<br>

`ChangeSessionIdAuthenticationStrategy`는 세션 고정 공격(Session Fixation)을 방어하기 위한 기능이다.

`ConcurrentSessionControlAuthenticationStrategy`를 이용하면 한 사용자에게 허용할 로그인 세션 수를 제한할 수 있다.

최대 세션 수를 초과했을 때 기존 세션을 만료 처리하거나 새로운 로그인을 거부하도록 설정할 수 있다.

<br>
<hr>

## 7. 인가와 예외 처리

<br>

Spring Security에서는 URL별 접근 권한을 설정할 수 있다.

- `permitAll()` : 인증 없이 접근 허용
- `authenticated()` : 인증된 사용자만 접근
- `hasRole("ADMIN")` : ADMIN 역할을 가진 사용자만 접근
- `hasAnyRole(...)` : 지정된 역할 중 하나를 가진 사용자에게 접근 허용

일반적으로 역할은 `ROLE_USER, ROLE_ADMIN`과 같은 GrantedAuthority로 표현된다.

`hasRole("ADMIN")`은 기본 역할 접두사 설정에서 ROLE_ADMIN을 확인한다.

<br>

### · 주요 인증·인가 예외 처리

#### AuthenticationEntryPoint

인증이 필요한 요청에서 사용자가 인증되지 않은 경우 응답을 처리한다. 일반적으로 HTTP 401을 사용한다.

#### AccessDeniedHandler

인증된 사용자가 필요한 권한을 가지고 있지 않을 때 응답을 처리한다. 일반적으로 HTTP 403을 사용한다.

Spring Security 필터에서 발생하는 보안 예외와 Controller 내부의 비즈니스 예외는 처리 경로가 다르다는 점에 주의한다.

<br>
<hr>

## 8. 로그아웃과 세션 무효화

<br>

#### · 일반 로그아웃

현재 사용자의 SecurityContext를 정리하고 해당 세션을 무효화한다.

#### · 강제 로그아웃

관리자가 특정 사용자의 세션을 검색하여 삭제한다.

Spring Session의 `FindByIndexNameSessionRepository`를 이용하면 사용자 식별자를 기준으로 활성 세션을 조회할 수 있다.

인증된 세션이 Redis에 저장되어 있다면, 해당 세션을 삭제하여 이후 요청에서 기존 인증 상태를 복원하지 못하도록 할 수 있다.

<br>
<hr>

## 9. 학습 핵심 정리 
- Spring Security는 인증과 인가를 담당한다.
- AuthenticationManager가 인증을 요청하며 AuthenticationProvider가 실제 인증을 수행한다.
- UserDetailsService는 사용자 정보를 조회하고 UserDetails는 인증에 필요한 사용자 정보를 제공한다.
- SecurityContext는 인증 결과를 담고 SecurityContextRepository는 이를 요청 사이에 유지한다.
- Spring Session과 Redis를 연동하면 여러 서버가 세션을 공유할 수 있다.
- SessionAuthenticationStrategy는 동시 로그인 제한, 세션 ID 변경, 세션 등록 등을 담당한다.
- CredentialsContainer는 인증 후 비밀번호 등 민감한 정보를 제거하는 데 사용한다.

<br>