<br>

# Day 1. Spring Controller, 공통 응답과 예외 처리

<br>

## 1. IoC와 DI

<br>

week1에서 미리 배웠던 내용이지만 중요한 내용이니 다시 한번 정리해보자.

Spring에서는 객체가 필요한 클래스마다 직접 `new`로 생성하기보다 **Spring Container가 객체를 생성하고 관리**하도록 한다.

```java
private final UserService userService = new UserService();
```

위와 같이 직접 생성하면 클래스가 객체 생성 방법까지 알고 있어야 하며, 여러 곳에서 같은 객체가 필요할 경우 객체 관리도 어려워진다.

Spring에서는 이러한 객체 생성과 관리의 책임을 Spring Container에 맡길 수 있다.

<br>

### · IoC (Inversion of Control)

**객체를 누가 생성하고 관리할 것인가?**

기존에는 개발자가 직접 객체를 생성했다면 Spring에서는 Spring Container가 객체의 생성과 생명주기를 관리한다.

```java
@Configuration
public class AppConfig {
    
    @Bean
    public UserService userService() {
        return new UserService();
    }
}
```
`@Bean`을 사용하면 반환하는 객체가 Spring Bean으로 등록된다.

> Spring Bean의 기본 Scope는 Singleton이다.
> 
> 따라서 하나의 Bean 인스턴스를 여러 곳에서 공유해서 사용할 수 있다.

### · DI (Dependency Injection)

**Spring이 관리하는 객체를 필요한 객체에 전달하는 과정이다.**

```java
@RestController
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

`UserController`가 직접 `UserService`를 생성하지 않고 외부에서 전달받는다.

즉,

```aiignore
Spring Container → UserService 생성 → UserController에 주입
```

하는 구조이다.

<br>
<hr>

## 2. @Component와 자동 Bean 등록

<br>

모든 객체를 `@Bean`으로 직접 등록하면 클래스가 많아질수록 설정 코드도 많아진다.

Spring에서는 Component Scan을 통해 Bean을 자동으로 등록할 수 있다.

```aiignore
@Component
├── @Controller
├── @Service
└── @Repository
```

각 어노테이션은 역할을 명확하게 표현하면서 동시에 Spring Bean으로 등록한다.

- `@Controller` : 요청을 처리하는 Controller
- `@Service` : 비즈니스 로직
- `@Repository` : DB 접근
- `@Component` : 그 외 일반적인 Spring Bean

REST API에서는 주로 `@RestController`를 사용한다.

```java
@RestController
public class UserController {
}
```

`@RestController`는 반환값을 JSON 등의 HTTP Response Body로 전달할 때 사용한다.

<br>
<hr>

## 3. Controller

<br>

Controller는 클라이언트의 HTTP 요청을 받아 적절한 로직을 호출하고 결과를 응답한다.

```java
@RestController
@RequestMapping("/users")
public class UserController {

}
```

`@RequestMapping("/users")`를 선언하면 해당 Controller의 기본 URL이 `/users`가 된다.

<br>

### · HTTP Methods

<table>
    <thead>
        <th>Method</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>GET</td>
            <td>데이터 조회</td>    
        </tr>
        <tr>
            <td>POST</td>
            <td>데이터 생성</td>    
        </tr>
        <tr>
            <td>PUT</td>
            <td>데이터 전체 수정</td>    
        </tr>
        <tr>
            <td>PATCH</td>
            <td>데이터 일부 수정</td>    
        </tr>
        <tr>
            <td>DELETE</td>
            <td>데이터 삭제</td>    
        </tr>
    </tbody>
</table>

<br>
<hr>

## 4. Controller에서 값 받기

<br>

### · @PathVariable

URL 경로의 값을 가져온다. 

```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    ...
}
```

```aiignore
요청: GET /users/1
```

`id`에는 `1`이 들어온다.

### · @RequestParam

Query String의 값을 하나씩 받을 때 사용한다.

```java
@GetMapping
public void getUsers(
        @RequestParam String name, @RequestParam Long age
) {
}
```
```aiignore
요청: GET /users?name=hong&age=20
```

### · @ModelAttribute

Query String 값이 여러 개라면 객체로 한 번에 받을 수도 있다.

```java
@GetMapping
public void getUsers(UserSearchRequest request) {
}

@Getter
@Setter
public class UserSearchRequest {
    
    private String name;
    private Long age;
}
```

```aiignore
GET /users?name=hong&age=20
```

이 경우 `UserSearchRequest`처럼 단순 타입(String, Long 등)이 아닌 객체 타입 파라미터는 Spring MVC가 자동으로 `@ModelAttribute`로 간주하여 처리한다.
따라서 `@ModelAttribute`를 명시하지 않아도 Query String의 값들이 객체의 필드에 바인딩된다.

`@ModelAttribute`가 생략 가능한 이유는 `@AllArgsConstructor`같은 Lombok 어노테이션 때문이 아니라, Spring MVC의 파라미터 바인딩 규칙 때문이다.

### · @RequestBody

HTTP Body의 데이터를 Java 객체로 변환할 때 사용한다.

```java
@PostMapping
public void create (@RequestBody UserRequest request) {
    
}
```

```aiignore
요청 Body: 
{
    "username": "hong",
    "email": "hong@test.com",
    "password": "1234"
}
```

주로 POST, PUT, PATCH처럼 데이터를 전달하는 요청에서 사용한다.

<br>
<hr>

## 5. Validation

<br>

잘못된 요청이 Controller 내부까지 들어오기 전에 DTO 단계에서 검증할 수 있다.

```java
@Getter
public class UserRequest {
    
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 2, max = 50)
    private String username;
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
```

Controller에서는 `@Valid`를 사용한다.

```java
@PostMapping
public void create(
        @Valid @RequestBody UserRequest request
) {
    
}
```

검증에 실패하면 `MethodArgumentNotValidException` 이 발생한다.

이 예외는 `@RestControllerAdvice`의 `@ExceptionHandler`에서 잡아 공통 응답 형식으로 변환할 수 있다.

참고로 `@RequestBody`가 아닌 `@ModelAttribute`(또는 어노테이션을 생략한 객체 바인딩)에 `@Valid`를 적용한 경우에는 `BindException`이 발생한다. 

<br>
<hr>

## 6. ResponseEntity와 ApiResponse

<br>

API 마다 응답 형식이 다르면 클라이언트가 데이터를 사용하기 불편하다.

따라서 다음과 같이 공통 응답 구조를 사용할 수 있다.

```aiignore
{
    "error": null,
    "data": {
        "id": 1,
        "name": "hong"
    }
}
```

<br>

### · ResponseEntity

HTTP 자체의 응답을 표현한다.

```aiignore
ResponseEntity
├── HTTP Status
├── Header
└── Body
```

예:
```java
return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
```

### · ApiResponse

우리 서비스에서 사용할 **Body의 공통 형식**을 정의한다.

```java
public class ApiResponse<T> {
    
    private Error error;
    private T data;
}
```
즉 둘은 같은 역할이 아니다.

```aiignore
ResponseEntity
└── ApiResponse
    ├── error
    └── data
```

<br>
<hr>

## 7. GlobalExceptionHandler

<br>

Controller마다 `try-catch`를 작성하면 동일한 예외 처리 코드가 반복된다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        return ApiResponse.fail(
                ex.getHttpStatus(),
                ex.getCode(),
                ex.getMessage()
        );
    }
}
```

`@RestControllerAdvice`를 사용하면 여러 Controller에서 발생하는 예외를 한 곳에 처리할 수 있다.

```aiignore
Request → Controller → Service → Exception 발생 
→ GlobalExceptionHandler → ApiResponse.fail() → HTTP Response
```

<br>
<hr>

## 8. DomainException

<br>

비즈니스 로직에서 발생할 수 있는 예외를 하나의 형식으로 표현할 수 있다.

```java
public enum DomainExceptionCode {
    
    INVALID_TOKEN,
    EXPIRED_TOKEN,
    NOT_FOUND_PRODUCT
}
```

실제로 예외를 발생시킬 때는 `DomainException`을 사용한다.

```java
throw new DomainException(
        DomainExceptionCode.NOT_FOUND_PRODUCT      
);
```

각 클래스의 역할을 구분하면 다음과 같다.

```aiignore
DomainExceptionCode → 어떤 에러인가?

DomainException → 실제로 발생시키는 예외

GlobalExceptionHandler → 발생한 예외를 잡아서 처리

ApiResponse → 클라이언트에게 전달할 응답 형태 

ResponseEntity → HTTP Status와 Body를 포함한 실제 HTTP 응답
```

처음 보면 클래스가 많아 보이지만 각각의 책임을 분리해둔 것이다.

<br>
<hr>

## 핵심 정리 

<br>

```aiignore
IoC → 객체 생성과 관리의 주체를 Spring으로 넘긴다.

DI → Spring이 관리하는 객체를 필요한 곳에 전달한다.

@RestController → HTTP 요청을 처리하고 데이터를 응답한다.

@PathVariable → URL 경로의 값을 받는다.

@RequestParam → Query String의 값을 받는다.

@RequestBody → HTTP Body의 데이터를 객체로 받는다.

@Valid → 요청 DTO를 검증한다.

ApiResponse → API Body 형식을 통일한다.

ResponseEntity → HTTP Status + Header + Body를 표현한다.

@RestControllerAdvice → 여러 Controller의 예외를 공통으로 처리한다.

DomainException → 도메인에서 발생한 예외를 표현한다.
```

```aiignore
Client
  ↓ HTTP Request
Controller
  ↓
DTO / Validation
  ↓
Service
  ↓
Response 또는 Exception
  ↓
ApiResponse / GlobalExceptionHandler
  ↓
Client
```