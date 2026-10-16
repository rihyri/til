<br>

# Week 05 - Day 04 : 학습 회고

<br>

이번 수업에서는 Spring Security를 이용한 사용자 인증과 Redis 기반 세션 관리에 대해 학습했다.

Spring Security의 인증 구조를 알아보고, UserDetails와 UserDetailsService를 직접 구현하며 DB에 저장된 사용자 정보로 로그인하는 과정을 살펴보았다.

또한 Spring Session과 Redis를 연동하며 여러 서버에서 세션을 공유하는 방법과 동시에 로그인 제한, 강제 로그아웃 처리에 대해서도 학습했다.

<br>

이번 과정에서는 생소한 인터페이스와 클래스가 많이 등장하여 각 구성 요소의 역할을 구분하는 것이 어려웠다.

특히 AuthenticationManger, AuthenticationProvider, UserDetailsService가 서로 어떻게 연결되어 인증을 처리하는지 이해하는 데 시간이 필요했다.

SessionAuthenticationStrategy 또한 여러 구현체가 등장하여 각각 어떤 시점에 실행되고 왜 필요한지 쉽게 이해되지 않았다.

<br>

다음과 같은 내용을 새롭게 이해하게 됐다.

### 1. Redis를 이용한 세션 공유

기존에는 세션이 서버 메모리에 저장된다는 사실만 알고 있었지만, 서버가 여러 대일 때 왜 문제가 발생하는지 알게 되었다.

모든 서버가 동일한 Redis를 세션 저장소로 사용하면 요청이 어느 서버로 전달되더라도 로그인 상태를 유지할 수 있다는 점을 이해했다.

### 2. UserDetails, CredentialsContainer, Serializable

처음에는 하나의 클래스에 여러 인터페이스를 구현하는 이유가 궁금했다.

학습 후 각 인터페이스가 서로 다른 역할을 담당한다는 것을 알게 되었다.

- UserDetails : Spring Security가 사용할 사용자 정보 제공
- CredentialsContainer : 인증 이후 민감한 정보 제거
- Serializable : JDK 직렬화를 통한 세션 저장 지원

### 3. SecurityContext와 세션

인증에 성공했다고 해서 로그인 상태가 자동으로 모든 요청에 유지되는 것은 아니라는 점도 배웠다.

인증 결과를 SecurityContext에 저장하고, 이를 세션에 저장해야 이후 요청에서도 인증 정보를 활용할 수 있다.

### 4. ClassLoader와 역직렬화

DevTools 사용 시 RestartClassLoader로 인해 역직렬화 문제가 발생할 수 있다는 것을 알게 되었다.

같은 이름의 클래스라도 서로 다른 ClassLoader로 로딩되면 다른 타입으로 취급될 수 있다는 점이 흥미로웠다.

<br>

Spring Security는 설정해야 할 클래스와 메서드가 많아 처음에는 외워야 하는 부분이 많다고 느꼈다.

하지만 각 구성 요소를 단순히 암기하기보다는 인증 과정에서 어떤 역할을 담당하는지 연결해서 이해하는 것이 중요하다고 생각한다.

특히 이번 수업은 이전에 학습한 Redis를 실제 사용자 인증 기능과 연결해볼 수 있었다는 점에서 의미가 있었다.

앞으로는 코드를 직접 실행해 보면서 로그인 요청이 인증과 세션 저장으로 이어지는 과정을 확인하고, 각 구성 요소의 역할을 조금 더 확실하게 이해하고 싶다. 

<br>

