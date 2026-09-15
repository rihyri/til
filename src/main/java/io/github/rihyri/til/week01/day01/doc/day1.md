<br>

# Day 1. Spring

<br>

Spring은 Java 애플리케이션을 만들 때 **객체 관리나 반복되는 공통 작업을 대신 처리해 주는 프레임워크**다.

직접 Java 코드로도 객체를 생성하고 웹 요청을 처리할 수 있지만, 애플리케이션이 커질수록 객체 간 관계나 설정을 관리하기 어려워진다.

Spring은 이러한 복잡함을 줄이기 위해 IoC, AOP, PSA 같은 핵심 개념을 제공한다.

<br>
<hr>

## IoC

<br>

IoC는 **Inversion of Control, 제어의 역전**을 의미한다.

일반 Java에서는 개발자가 직접 객체를 생성한다.

```java
GreetingService service = new GreetingService();
```

하지만 Spring에서는 객체 생성과 관리를 Spring에게 맡길 수 있다.

```java
@Service
public class GreetingService {
}
```

Spring이 관리하는 객체를 **Bean**이라고 하고, 이러한 Bean들을 관리하는 공간을 **Spring Container**라고 한다.

즉,

> 객체를 개발자가 직접 생성하고 관리하는 것이 아니라 Spring이 대신 관리하는 것이 IoC이다.

필요한 객체를 다른 객체에 넣어주는 것을 **DI(Dependency Injection)**이라고 한다.

<br>
<hr>

## AOP

<br>

AOP는 **Aspect Oriented Programming, 관점 지향 프로그래밍**이다.

여러 기능에서 반복되는 코드를 하나로 분리하기 위해 사용한다.

예를 들어 다음 기능들은 여러 Service에서 반복될 수 있다.

- 로그 기록
- 트랜잭션
- 권한 확인
- 실행 시간 측정

이런 기능을 비즈니스 로직과 분리하면 Service는 자신의 핵심 역할에 집중할 수 있다.

```aiignore
공통 로직 → Service 핵심 로직
```

Spring의 `@Transactional`도 AOP가 활용되는 대표적인 예시다.

<br>
<hr>

## PSA

<br>

PSA는 **Portable Service Abstraction**의 약자다.

Spring이 복잡한 기술을 한 번 감싸서 개발자가 **일관된 방식으로 사용할 수 있도록 해주는 것**이라고 이해하면 된다.

예를 들어 Servlet을 직접 사용하면 HTTP 요청을 직접 처리해야 한다.

Spring MVC에서는 다음처럼 간단하게 작성할 수 있다.

```java
@GetMapping("/hello")
public String hello() {
    return "Hello";
}
```

개발자는 Servlet의 내부 동작을 직접 다루지 않아도 된다.

대표적인 PSA의 예로는 Spring MVC, Spring Transaction 등이 있다.

<br>
<hr>

## Tomcat과 Servlet

<br>

**Servlet**은 Java에서 HTTP 요청과 응답을 처리하기 위한 기술이다.

```aiignore
HTTP 요청 → Servlet → HTTP 응답
```

그리고 Servlet을 실행하고 관리하는 프로그램을 **Servlet Container**라고 한다.

대표적인 Servlet Container가 **Tomcat**이다.

Spring Boot에서는 일반적으로 내장 Tomcat을 사용하기 때문에 별도로 Tomcat을 설치하지 않아도 애플리케이션을 실행할 수 있다.

<br>
<hr>

## Tomcat과 Spring Container의 차이

<br>

두 가지의 역할은 다르다.

```aiignore
Tomcat → HTTP 요청과 Servlet 관리

Spring Container → Bean 생성, 관리, 의존성 주입
```

Spring MVC에서는 `DispatcherServlet`이라는 Servlet이 요청을 먼저 받고 적절한 Controller를 찾아준다.

전체 흐름을 간단하게 보면 다음과 같다.

```aiignore
Client → Tomcat → DispatcherServlet → Controller → Service
```

<br>
<hr>

## @SpringBootApplication

<br>

Spring Boot의 시작 클래스에는 보통 다음 어노테이션이 붙는다.

```java
@SpringBootApplication
public class LessonApplication {
}
```

`@SpringBootApplication`은 크게 다음 기능을 포함한다.

```aiignore
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

특히 `@ComponentScan`을 통해 `@Controller`, `@Service`, `@Component` 등이 붙은 클래스를 찾아 Spring Bean으로 등록한다.

<br>
<hr>

## SpringApplication.run()

<br>

Spring Boot 애플리케이션은 다음 코드에서 실행한다.

```java
SpringApplication.run(LessonApplication.class, args);
```

실행 과정을 간단하게 보면 다음과 같다.

```aiignore
SpringApplication.run() → Spring Container 생성 → Bean 탐색 및 등록
→ 의존성 주입 → Tomcat 실행 → HTTP 요청 대기
```

결국 Spring Boot의 `main()` 메서드는 단순해 보이지만, 이 한줄을 통해 Spring 애플리케이션에 필요한 환경이 만들어진다.

<br>
<hr>

## 마치며

<br>


Spring은 개발자가 객체 관리와 반복적인 기반 작업을 모두 직접 처리하지 않도록 도와준다.

```aiignore
IoC → 객체 관리를 Spring에게 맡긴다.

AOP → 반복되는 공통 로직을 분리한다.

PSA → 복잡한 기술을 일관된 방식으로 사용할 수 있게 한다. 
```