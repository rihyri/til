<br>

# Day 2. Spring Boot 웹 애플리케이션과 JPA

<br>

오늘은 Spring Boot 웹 애플리케이션 구조와 Spring Container, Layered Pattern을 배우고 JPA와 Flyway를 통해 데이터베이스를 다루는 방법을 알아봤다.

<br>
<hr>

## 1. 웹 서버와 웹 애플리케이션

<br>

웹 서버는 클라이언트의 HTTP 요청을 받고 응답을 전달하는 역할을 한다. 

반면 웹 애플리케이션은 단순히 요청을 전달하는 것을 넘어 회원가입, 로그인, 게시글 작성 같은 **실제 서비스 로직을 수행하는 프로그램**이다.

Spring Boot에서는 보통 다음과 같은 형태로 동작한다.

<br>

```aiignore
    Client
      ↓ HTTP Request
    Tomcat
      ↓
  Spring Boot
      ↓
  Controller → Service → Repository
      ↓
   Database
```

`spring-boot-starter-web`을 사용하면 기본적으로 Tomcat이 내장되어 있기 때문에 별도로 Tomcat을 설치하지 않아도 애플리케이션을 실행할 수 있다. 

중요한 점은 **Tomcat 자체가 Spring은 아니라는 것**이다.

Tomcat은 HTTP 요청을 받아 Servlet을 실행할 수 있게 해주는 Servlet Container이고, Spring은 그 위에서 우리가 만든 애플리케이션 로직을 실행한다.

> Spring Boot 버전에 따라 사용할 수 있는 내장 서버가 다를 수 있다.

<br>
<hr>

## 2. Spring Container

<br>

Spring Container는 Spring에서 객체를 생성하고 관리하는 공간이다.

개발자가 직접

```java
new UserService();
```

처럼 객체를 계속 생성하는 대신 Spring에게 객체 관리를 맡길 수 있다.

Spring이 관리하는 객체를 **Bean**이라고 한다.

```aiignore
* Spring Container

┌───────────────────────┐ 
│  UserController Bean  │ 
│  UserService Bean     │ 
│  UserRepository Bean  │ 
└───────────────────────┘
```

Spring Container는 Bean의 

- 생성
- 관리
- 의존성 주입 (DI)
- 생명주기

등을 담당한다.

쉽게 생각하면 **Spring이 객체를 보관하고 관리하는 공간**이라고 이해하면 된다.

<br>
<hr>

## 3. Layered Pattern

<br>

Layered Pattern은 프로그램의 역할을 여러 계층으로 나누어 관리하는 구조다.

Spring에서는 보통 다음과 같이 나눈다.

<br>

<table>
    <thaed>
        <th>Layer</th>
        <th>역할</th>
    </thaed>
    <tr>
        <td>Controller</td>
        <td>API 요청과 응답 관리</td>
    </tr>
    <tr>
        <td>Service</td>
        <td>비즈니스 로직 처리</td>
    </tr>
    <tr>
        <td>Repository</td>
        <td>데이터베이스 접근</td>
    </tr>
    <tr>
        <td>Entity</td>
        <td>DB 테이블과 매핑되는 객체</td>
    </tr>
</table>

<br>

요청 흐름은 보통 다음과 같다.

```aiignore
Client → Controller → Service → Repository → Database
```

예를 들어 회원을 조회한다면

```aiignore
* GET /members/1

Controller : "1번 회원 조회 요청이 들어왔네"
                   ↓
Service : "회원 조회 로직을 처리해야겠다"
                   ↓
Repository : "DB에서 id가 1인 회원을 조회하자"
                   ↓
Database
```

역할을 분리하면 코드가 커져도 수정하거나 관리하기 쉬워진다.

<br>
<hr>

## 4. @Configuration

<br>

`@Configuration`은 **Spring 설정을 정의하는 클래스라는 것을 알려주는 어노테이션**이다.

특히 `@Bean`과 함께 사용해서 Spring Container에 직접 객체를 등록할 수 있다.

```java
@Configuration
public class AppConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

이렇게 작성하면

```java
new BCryptPasswordEncoder();
```

로 생성된 객체를 Spring이 관리한다.

즉,

```aiignore
@Configuration → Spring 설정 클래스
@Bean → Spring Container가 관리할 객체
```

라고 이해하면 된다.

<br>
<hr>

## 5. ORM

<br>

ORM은 **Object Relational Mapping**의 약자다.

Java의 객체와 관계형 데이터베이스의 테이블을 연결해주는 기술이다.

기존에는 SQL을 직접 작성해서 데이터를 조회했다.

```sql
SELECT * FROM member WHERE id = 1;
```

MyBatis 역시 SQL을 직접 작성하는 SQL Mapper 방식이다.

프로젝트가 커지면 SQL 작업량이 많아지고 객체와 SQL 결과를 연결하는 작업도 반복된다.

ORM을 사용하면 객체 중심으로 데이터베이스를 다룰 수 있다.

```java
Member member = memberRepository.findById(1L);
```

SQL을 직접 작성하는 양을 줄이고 객체 중심으로 개발할 수 있다는 장점이 있다.

<br>
<hr>

## 6-1. JPA

<br>

JPA는 Java에서 ORM을 사용하기 위한 **표준 인터페이스**다.

JPA 자체가 실제로 DB와 통신하는 것은 아니고 구현체가 필요하다.

Spring Boot에서는 대표적으로 Hibernate를 사용한다.

```aiignore
개발자 → JPA → Hibernate → JDBC → Database
```

정리하면 다음과 같다.

```aiignore
JPA → ORM 사용 방법을 정의한 표준
Hibernate → JPA를 실제로 구현한 라이브러리
```

<br>
<hr>

## 6-2. Hibernate

<br>

Hibernate는 **JPA를 실제로 구현한 ORM 프레임워크**다.

JPA는 Java에서 ORM을 어떻게 사용할지 정의한 표준이기 때문에 JPA만으로는 실제 SQL을 생성하거나 DB와 통신할 수 없다.

이 역할을 실제로 수행하는 대표적인 구현체가 Hibernate다.

```aiignore
개발자 → JPA "ORM은 이런 방식으로 사용해야 해"
→ Hibernate "그 규칙에 맞춰 실제로 처리할게" → JDBC → Database
```

예를 들어 다음과 같이 Repository를 사용하면 

```java
memberRepository.findById(1);
```

개발자가 직접 SELECT SQL을 작성하지 않아도 Hibernate가 Entity 정보를 이용해 필요한 SQL을 생성한다.

```aiignore
SELECT ... FROM members WHERE id = ?;
```

Entity를 저장할 때도 마찬가지다.

```java
memberRepository.save(member);
```

Hibernate가 Entity의 매핑 정보를 확인하고 INSERT SQL을 생성한다.

```aiignore
INSERT INTO members (...)
VALUES (...);
```

즉 각각의 역할을 정리하면 다음과 같다.

```aiignore
ORM → 객체와 관계형 DB를 연결하는 개념

JPA → Java에서 ORM을 사용하기 위한 표준

Hibernate → JPA를 실제로 구현하여 SQL 생성, Entity 관리 등을 수행

Spring Data JPA → JPA를 더 편리하게 사용할 수 있도록 Repository 기능 제공
```

특히 `spring-boot-starter-data-jpa`를 사용하면 Hibernate가 기본 JPA 구현체로 함께 포함되기 때문에 별도로 Hibernate 의존성을 추가할 필요가 없다. 

<br>
<hr>

## 7. JPA Entity

<br>

`@Entity`는 이 클래스가 JPA에서 관리하는 **Entity 객체**라는 것을 의미한다.

Entity는 일반적으로 데이터베이스 테이블과 매핑된다.

```java
@Entity
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nickname;
}
```

주의할 점은 **`@Entity`를 작성했다고 무조건 테이블이 생성되는 것은 아니다라는 것**이다.

Hibernate의 `ddl-auto` 설정에 따라 테이블 생성 여부가 결정된다.

```aiignore
spring: 
  jpa: 
    hibernate: 
      ddl-auto: create
```

`create`, `update` 등을 사용하면 Entity 정보를 기반으로 Hibernate가 DB 스키마를 변경할 수 있다.

<br>
<hr>

## 8. Flyway

Flyway는 **데이터베이스의 변경 이력을 관리하는 도구**다.

예를 들어 처음에는 회원 테이블만 있었다고 해보자.

```aiignore
V1 → members 테이블 생성
V2 → phone 컬럼 추가
V3 → nickname 컬럼 길이 변경
```

Flyway에서는 이런 변경을 파일로 관리한다.

```aiignore
* db/migration

V1__create_members.sql
V2__add_phone_to_member.sql
V3__change_nickname_length.sql
```

예를 들어

```aiignore
ALTER TABLE members ADD COLUMN phone VARCHAR(20);
```

처럼 SQL을 직접 작성한다.

Flyway는 어떤 파일까지 실행됐는지 기록하기 때문에 서버나 개발자의 환경이 달라도 같은 DB 구조를 만들기 쉽다.

<br>

### 그렇다면 @Entity와 Flyway의 차이는?

<br>

가장 중요한 부분이다.

```aiignore
* @Entity
Java 객체 ↔ DB 테이블의 관계 정리

* Flyway
DB 구조 변경 이력 관리
```

예를 들어 새로운 `phone` 필드가 추가되었다면

```aiignore
@Column
String phone;
```

Entity에도 추가하고,

```aiignore
ALTER TABLE members
ADD COLUMN phone VARCHAR(20);
```

Flyway Migration도 작성할 수 있다.

즉 둘을 같이 사용할 수 있다.

개발 단계에서는 `ddl-auto=create`나 `update`를 이용해 빠르게 실습할 수도 있지만, DB 변경 이력을 명확하게 관리해야 하는 프로젝트에서는 Flyway를 사용하려는 이유가 생긴다.

<br>
<hr>

## 마치며

<br>

```aiignore
Tomcat → HTTP 요청을 받고 Servlet을 실행하는 Container

Spring Container → Spring Bean을 생성하고 관리

Layered Pattern → Controller / Service / Repository 역할 분리

ORM → Java 객체와 DB 테이블을 연결

JPA → Java ORM 표준

Hibernate → JPA 구현체 

@Entity → Java 객체와 DB 테이블 매핑

Flyway → DB 변경 이력을 SQL 파일로 관리
```

오늘 내용에서 중요한 흐름은 다음과 같다. 

```aiignore
Request → Tomcat → Spring → Controller → Service → Repository 
→ JPA / Hibernate → Database
```