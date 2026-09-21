<br>

# Week 1. Spring Boot & JPA

<br>

1주차에는 Spring의 기본 동작 원리부터 Spring Boot 웹 애플리케이션의 구조, JPA를 이용한 데이터베이스 접근과 Entity 연관관계까지 학습했다.

각각의 개념을 따로 외우기보다는 하나의 요청이 들어왔을 때 Spring 애플리케이션이 내부에서 어떤 흐름으로 처리되는지 이해하는 것이 중요하다.

전체적인 흐름은 다음과 같다.

```aiignore
Client
  ↓  HTTP Request  
Tomcat
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
JPA / Hibernate
  ↓
Database
```

<br>
<hr>

## 1. Spring의 핵심 개념

<br>

Spring을 이해할 때 가장 먼저 알아야 하는 개념은 `IoC`, `DI`, `AOP`, `PSA`이다.

<br>

### · IoC와 DI

일반적인 Java 코드에서는 개발자가 직접 객체를 생성한다.

```java
UserRepository userRepository = new UserRepository();
UserService service = new UserService(repository);
```

Spring에서는 객체의 생성과 관리를 Spring에게 맡길 수 있다.

Spring이 관리하는 객체를 **Bean**, Bean을 관리하는 공간을 **Spring Container**라고 한다.

```aiignore
IoC → 객체의 생성과 관리 권한을 Spring에게 맡긴다.

DI → Spring이 필요한 객체를 다른 객체에 주입해준다.
```

Service가 Repository를 직접 생성하는 것이 아니라 Spring이 만들어 둔 Repository Bean을 전달받는 것이 대표적인 DI이다.

### · AOP

AOP는 여러 기능에서 반복되는 공통 로직을 핵심 비즈니스 로직과 분리하는 방법이다.

대표적으로 다음과 같은 기능이 있다.

- 트랜잭션
- 로그
- 권한 확인
- 실행 시간 측정

Spring의 `@Transactional` 역시 AOP를 활용하는 대표적인 기능이다.

### · PSA
PSA는 복잡한 기술을 Spring이 한 번 감싸 개발자가 일관된 방식으로 사용할 수 있도록 도와주는 것이다.

예를 들어 Servlet을 직접 다루지 않고 Spring MVC의 어노테이션만으로 HTTP 요청을 처리할 수 있다.

```java
@GetMapping("/users")
public List<UserResponse> getUsers() {
    return userService.getUsers();
}
```

<br>
<hr>

# 2. Tomcat과 Spring Container

Tomcat과 Spring Container는 서로 다른 역할을 한다.

```aiignore
Tomcat → HTTP 요청을 받고 Servlet을 실행하는 Servlet Container

Spring Container → Bean을 생성하고 관리하며 의존성을 주입
```

Spring MVC에서는 `DispatcherServlet`이 요청을 가장 먼저 받아 적절한 Controller로 전달한다.

```aiignore
Client → Tomcat → DispatcherServlet → Controller
```

Spring Boot에서는 내장 Tomcat을 사용하기 때문에 별도의 Tomcat 설치 없이 웹 애플리케이션을 실행할 수 있다.

<br>
<hr>

## 3. Layered Pattern

Spring 애플리케이션에서는 역할에 따라 코드를 여러 계층으로 나누어 관리한다.

<table>
    <thead>
        <th>Layer</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>Controller</td>
            <td>HTTP 요청과 응답 처리</td>
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
    </tbody>
</table>

예를 들어 회원을 조회한다면 다음과 같은 흐름으로 동작한다.

```aiignore
* GET /users/1

Controller "1번 회원 조회 요청이 들어왔다."
                ↓
Service "회원 조회 로직을 수행한다."
                ↓
Repository "DB에서 회원을 조회한다."
                ↓
            Database
```

각 계층의 역할을 분리하면 코드가 커져도 변경과 관리가 쉬워진다.

<br>
<hr>

## 4. ORM, JPA, Hibernate, Spring Data JPA

<br>

처음에는 각각의 개념이 비슷해 보여 헷갈릴 수 있지만 역할이 다르다.

```aiignore
ORM → 객체와 관계형 데이터베이스를 연결하는 개념

JPA → Java에서 ORM을 사용하기 위한 표준

Hibernate → JPA를 실제로 구현한 ORM 프레임워크 

Spring Data JPA → JPA를 편리하게 사용할 수 있도록 repository 기능 제공
```

실제 동작을 간단하게 표현하면 다음과 같다.

```aiignore
개발자 → Spring Data JPA → JPA → Hibernate → JDBC → Database
```

예를 들어

```java
userRepository.findById(1L);
```

처럼 작성하면 개발자가 직접 `SELECT` SQL을 작성하지 않아도 Hibernate가 Entity 정보를 이용하여 SQL을 생성한다.

<br>
<hr>

## 5. Entity와 Flyway

<br>

`@Entity`는 Java 객체와 데이터베이스 테이블의 관계를 정의한다.

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
}
```
Entity와 DB 스키마 변경 관리는 서로 다른 역할이다.

```aiignore
@Entity → Java 객체와 DB 테이블의 매핑

Flyway → DB 구조의 변경 이력을 관리
```

예를 들어 `phone` 컬럼이 추가된다면 Entity에도 필드를 추가하고 Flyway Migration도 작성할 수 있다.

```aiignore
V1__create_users.sql
V2__add_phone_to_users.sql
```

Flyway를 사용하면 서버나 개발 환경이 달라도 동일한 데이터베이스 구조를 유지하기 쉽다.

<br>
<hr>

## 6. 의존성 주입은 생성자 주입

<br>

Spring에서는 필드 주입보다 **생성자 주입 + final** 조합을 주로 사용한다.

```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Lombok의 `@RequiredArgsConstructor`를 사용하면 코드를 더 간단하게 작성할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
}
```

`final`을 사용하면 객체가 생성되는 시점부터 필요한 의존성이 존재하도록 만들 수 있고 이후 다른 객체로 변경되는 것도 막을 수 있다.

<br>
<hr>

## 7. Entity를 안전하게 사용하기

<br>

Entity에 전역 `@Setter`를 사용하면 모든 값을 외부에서 자유롭게 변경할 수 있다.

```java
user.setName("");
```

따라서 필요한 변경 동작을 의미 있는 메서드로 만드는 방법을 사용할 수 있다.

```java
public void changeName(String name) {
    
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
    }       
    
    this.name = name;
}

user.changeName("홍길동");
```
<br>

### Entity와 기본 생성자

JPA Entity에는 기본 생성자가 필요하다.

직접 생성자를 작성했다면 Lombok의 `@NoArgsConstructor`를 사용할 수 있다.

```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    private Long id; 
    
    private String name;
    
    public User(String name) {
        this.name = name;
    }
}
```

기본 생성자를 외부에서 임의로 사용하는 것을 막기 위해 `protected`로 제한하는 방법을 사용할 수 있다.

<br>
<hr>

## 8. JPA 연관관계

<br>

### N : 1 

여러 Order가 하나의 User를 바라본다면 Order 입장에서 `@ManyToOne` 관계이다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

연관관계는 필요한 방향을 중심으로 설계하고 무조건 양방향 관계를 만드는 것은 피하는 것이 좋다.

예를 들어 특정 User의 Order 목록이 필요하다면 다음과 같이 Repository에서 조회할 수도 있다.

```java
List<Order> findAllByUser_Id(Long userId);
```

여기서 `User_Id` 는

```aiignore
Order → user → id
```

처럼 연관 객체의 필드까지 탐색한다는 의미이다.

### N : N

N:N 관계를 `@ManyToMany`로 바로 연결할 수도 있지만 중간 Entity를 두면 관계에 대한 정보를 함께 관리할 수 있다.

```aiignore
User → UserProduct → Product
```

예를 들어 `UserProduct`에는 다음과 같은 정보를 추가할 수 있다.

```aiignore
구매날짜
수량
가격
상태
```

따라서 N 관계를 `N : N` 그대로 관리하기보다

```aiignore
User 1 : N UserProduct
Product 1 : N UserProduct
```

처럼 두 개의 N:1 관계로 풀어서 표현할 수 있다.

<br>
<hr>

## 9. Self-Reference

<br>

자기 참조는 하나의 Entity가 같은 Entity를 다시 참조하는 방식이다.

카테고리, 메뉴, 댓글처럼 계층 구조가 필요한 경우 자주 사용할 수 있다.

```aiignore
id | name | parent_id 
----------------------- 
1 | 의류   | null 
2 | 여성복 | 1 
3 | 남성복 | 1
```

구조를 표현하면 다음과 같다.

```aiignore
의류 
├─ 여성복 
└─ 남성복
```

JPA에서는 다음처럼 표현할 수 있다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Category parent;
```

`parent_id = null`이면 최상위 Root 데이터이고 특정 `parent_id`를 가지고 있다면 해당 데이터를 부모로 가지는 Child 데이터이다.

<br>
<hr>

## 10. DTO를 사용하는 이유

<br>

Entity는 데이터베이스와 매핑되는 객체이고 DTO는 계층 사이에서 필요한 데이터를 전달하는 객체이다.

API 응답으로 Entity 자체를 그대로 반환하기보다는 필요한 값만 DTO로 변환하여 전달할 수 있다.

```aiignore
Client
  ↓ Request DTO
Controller
  ↓
Service
  ↓
Repository
  ↓
Entity
  ↓
Service에서 DTO 변환
  ↓
Response DTO
  ↓
Client
```

여러 Entity를 DTO 목록으로 변환할 때 Stream으로 사용할 수도 있다.

```java
return users.stream()
        .map(user -> new UserResponse(
                user.getId(),
                user.getName()
        ))
        .toList();
```

즉

```aiignore
List<Entity> → stream() → map() → toList() → List<DTO>
```

과정이라고 이해할 수 있다.

<br>
<hr>

## 11. LAZY와 EAGER

<br>

JPA에서는 연관된 Entity를 언제 조회할 것인지 결정할 수 있다.

<br>

### EAGER

연관 데이터를 Entity 조회 시 바로 가져온다.

```java
@ManyToOne(fetch = FetchType.EAGER)
```

사용하지 않는 연관 데이터까지 조회될 수 있다는 단점이 있다.

### LAZY

실제로 연관 데이터가 필요한 시점에 조회한다.

```java
@ManyToOne(fetch = FetchType.LAZY)
```

연관관계에서는 `LAZY`를 명시적으로 사용하는 경우가 많다.

하지만 중요한 것은

```aiignore
LAZY = N + 1 해결
```

이 아니라는 것이다.

<br>
<hr>

## 12. N+1 문제

<br>

N+1 문제는 데이터를 한 번 조회한 뒤 연관 데이터를 가져오기 위한 쿼리가 반복적으로 발생하는 문제이다.

예를 들어 Order 3개를 조회했다고 하자.

```aiignore
Order 전체 조회    → 1 Query 
Order1의 User 조회 → 1 Query 
Order2의 User 조회 → 1 Query 
Order3의 User 조회 → 1 Query

총 4 Query
```

데이터가 많아질수록 추가 Query 역시 많아질 수 있다.

특히 LAZY 연관 데이터를 반복문에서 하나씩 접근할 때 주의해야 한다.

<br>
<hr>

## 13. N+1 해결 방법

<br>

### Fetch Join

필요한 연관 Entity를 처음 Query에서 함께 조회한다.

```java
@Query("""
        SELECT o
        FROM ORDERS o
        FETCH JOIN o.user
        """)
List<Order> findAllWithUser();

Order 조회 + User 조회 → 하나의 Query에서 함께 조회
```

### BatchSize

연관 데이터를 하나씩 조회하는 대신 일정 개수씩 묶어서 조회한다.

```aiignore
* 기존

1 → SELECT
2 → SELECT
3 → SELECT

* Batch

1, 2, 3 → SELECT ... WHERE id IN (...)
```

Fetch Join과 BatchSize의 차이는 다음처럼 이해할 수 있다.

```aiignore
Fetch Join → 처음부터 필요한 연관 데이터를 함께 조회

BatchSize → 연관 데이터가 필요한 시점에 여러 데이터를 묶어서 조회
```

<br>
<hr>

## 1주차 핵심 흐름 

<br>

이번 주에 배운 내용을 하나의 흐름을 연결하면 다음과 같다.

```aiignore
1. Client가 HTTP 요청을 보낸다.
            ↓
2. Tomcat이 요청을 받는다.
            ↓
3. DispatcherServlet이 적절한 Controller를 찾는다.
            ↓
4. Controller가 Service를 호출한다. 
            ↓
5. Service가 비즈니스 로직을 처리한다.
            ↓
6. Repository를 통해 데이터를 조회한다.
            ↓
7. Spring Data JPA / Hibernate가 SQL을 생성한다.
            ↓
8. Database에서 데이터를 조회한다.
            ↓
9. 조회된 Entity를 필요한 DTO로 변환한다.
            ↓
10. Controller가 Client에게 응답한다. 
```

<br>
<hr>

## 마치며

<br>

1주차에서 가장 중요한 것은 각각의 어노테이션을 외우는 것보다 **Spring 애플리케이션 전체 흐름을 이해하는 것**이다.

```aiignore
Spring → 객체의 생성과 관리를 담당

Controller / Service / Repository → 역할에 따라 코드 분리

JPA → Java 객체를 중심으로 DB 접근

Entity → DB 테이블과 객체를 연결

DTO → 외부에 필요한 데이터만 전달

연관관계 → 객체 사이의 관계 표현

LAZY / Fetch Join / BatchSize → 연관 데이터를 언제, 어떻게 조회할지 결정
```

결국 1주차에 배운 내용들은 모두

```aiignore
"Spring에서 객체를 어떻게 관리하고, 요청을 어떻게 처리하며,
데이터베이스의 데이터를 어떻게 객체로 다룰 것인가?"
```

라는 하나의 흐름으로 연결된다.

<br>

