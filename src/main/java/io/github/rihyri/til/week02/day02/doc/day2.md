<br>

# Day 2. Validation, Service Layer, Builder & Mapstruct

<br>

## 1. Validation

<br>

Validation은 클라이언트가 전달한 값이 애플리케이션에서 요구하는 형식과 규칙을 만족하는지 검증하는 과정이다.

Spring Boot에서는 `spring-boot-starter-validation`을 통해 Bean Validation을 사용할 수 있다.

```java
@NotBlank
private String name;

@Email
private String email;

@Positive
private int age;
```

자주 사용하는 Validation 어노테이션은 다음과 같다.

<table>
    <thead>
        <th>어노테이션</th>
        <th>의미</th>
    </thead>
    <tr>
        <td>@NotNull</td>
        <td>null 허용 X</td>
    </tr>
    <tr>
        <td>@NotEmpty</td>
        <td>null, 빈 문자열/컬렉션 허용 X</td>
    </tr>
    <tr>
        <td>@NotBlank</td>
        <td>null, 빈 문자열, 공백 문자열 허용 X</td>
    </tr>
    <tr>
        <td>@Size</td>
        <td>문자열 또는 컬렉션 크기 제한</td>
    </tr>
    <tr>
        <td>@Min, @Max</td>
        <td>숫자 최소/최대값</td>
    </tr>
    <tr>
        <td>@Positive</td>
        <td>양수만 허용</td>
    </tr>
    <tr>
        <td>@PositiveOrZero</td>
        <td>0 이상 허용</td>
    </tr>
    <tr>
        <td>@Email</td>
        <td>이메일 형식 검증</td>
    </tr>
    <tr>
        <td>@Pattern</td>
        <td>정규식을 이용한 형식 검증</td>
    </tr>
    <tr>
        <td>@Future, @FutureOrPresent</td>
        <td>미래 날짜 검증</td>
    </tr>
    <tr>
        <td>@Past, @PastOrPresent</td>
        <td>과거 날짜 검증</td>
    </tr>
</table>

문자열을 필수값으로 받을 때는 일반적으로 `@NotBlank`를 사용하면 `null`, `""`, `" "`까지 함께 막을 수 있다.

Controller에서는 `@Valid`를 사용하여 Request DTO에 선언한 검증을 실행한다.

```java
@PostMapping
public UserResponse create (@Valid @RequestBody UserCreateRequest request) {
    return userService.createUser(request);
}
```

흐름은 다음과 같다.

```aiignore
HTTP Request → @RequestBody → @Valid → DTO Validation → Controller → Service
```

즉 잘못된 요청을 Service까지 전달하기 전에 Controller 진입 단계에서 걸러낼 수 있다.

<br>
<hr>

## 2. Lombok

<br>

Lombok은 Spring의 기능이 아니라 **Java 코드를 간결하게 작성하기 위한 라이브러리**다.

대표적으로 다음과 같은 어노테이션을 사용한다.

```aiignore
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
```

<br>

### '@Data'를 주의해서 사용하는 이유

`@Data`는 여러 Lombok 기능을 한 번에 제공한다.

```aiignore
@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
```

편리하지만 필요하지 않은 기능까지 함께 적용된다는 문제가 있다.

특히 Entity에 `@Setter`가 열려 있으면 어디서든 Entity의 상태를 변경할 수 있다.

```java
user.setNickname("");
```

대신 변경 목적이 드러나는 메서드를 만드는 방법을 사용할 수 있다.

```java
public void changeNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
        throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
    }
    
    this.nickname = nickname;
}

user.changeNickname("홍길동");
```

`setNickname()`보다 **어떤 행위를 수행하는 코드인지 명확하게 표현할 수 있다.**

### `ToString`과 **Entity**

양방향 연관관계를 가진 Entity에서 `toString()`이 서로의 Entity를 계속 출력하면 순환 참조가 발생할 수 있다.

따라서 Entity 전체에 무분별하게 `@ToString`을 적용하지 않도록 주의한다.

### Jackson과 함께 사용할 수 있는 어노테이션

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
```
값이 `null`인 필드는 JSON 응답에서 제외한다.

```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createdAt;
```

날짜를 원하는 문자열 형식으로 JSON에 반환할 수 있다.

<br>
<hr>

## 3. Service 계층

<br>

Service는 단순히 Repository를 호출하는 클래스가 아니다.

Service의 주요 역할은 다음과 같다.

```aiignore
1. 비즈니스 로직 처리
2. 트랜잭션 관리
3. Controller와 Repository 사이의 역할 분리
```

예를 들어 회원가입이라는 하나의 행위에는 여러 작업이 포함될 수 있다.

```aiignore
* 회원가입

이메일 중복 확인 → 비밀번호 암호화 → User 생성 → DB 저장 → Response 변환 
```

이 작업들을 하나의 **비즈니스 행위**로 묶는 곳이 Service이다.

```java 
@Transactional
public UserResponse createUser(UserCreateRequest request) {
    
    // 이메일 중복 확인 ...
    
    // 비밀번호 암호화 ...
    
    // Entity 생성 ...
    
    // DB 저장 ...
    
    // Response 변환 ...
}
```

따라서 트랜잭션 역시 여러 작업을 하나의 실행 단위로 관리하는 Service 계층에서 적용하는 경우가 많다.

Repository가 단순히 데이터를 조회하는 역할이라면 Service는 **그 조회와 저장을 조합하여 하나의 의미 있는 행위를 만드는 역할**이라고 이해할 수 있다.

<br>
<hr>

## 4. `get`과 `find`

<br>

메서드 이름을 정할 때 `get`과 `find`를 구분하면 코드의 의도를 조금 더 명확하게 나타낼 수 있다.

```java
getUserById(userId);
```

`get`은 해당 데이터가 존재해야 한다는 의미로 사용할 수 있다.

```java
findUserByEmail(email);
```

`find`는 데이터가 존재하지 않을 수도 있다는 의미로 사용할 수 있다.

예를 들어 Repository에서는 다음과 같이 `Optional`을 반환할 수 있다.

```java
public User getUser(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> 
                    new IllegalArgumentException("사용자를 찾을 수 없습니다.");
            );
}
```
다만 `get`과 `find`의 구분은 Spring이 강제하는 규칙이 아니라 **코드의 의도를 표현하기 위한 네이밍 규칙**이다.

<br>
<hr>

## 5. Service끼리 무분별하게 참조하지 않기

<br>

다른 도메인의 데이터가 필요하다는 이유로 Service끼리 계속 의존하기 시작하면 다음과 같은 구조가 만들어질 수 있다.

```aiignore
UserService → OrderService → UserService 
```

결국 서로가 서로를 주입받는 순환 참조가 발생할 가능성이 커진다.

단순 데이터 조회가 필요한 경우 Service를 Repository처럼 사용하기보다 필요한 Repository에 직접 접근하는 방법을 사용한다.

```java
@Service
@RequiredArgsConstructor 
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
}
```

즉 Service를 단순한 데이터 제공 객체로 만드는 것이 아니라 **Service가 담당하는 비즈니스 행위를 중심으로 설계한다.**

<br>
<hr>

## 6. Builder 패턴

<br>

Setter를 이용하면 객체가 생성된 이후에도 값을 자유롭게 변경할 수 있다.

```java
User user = new User();

user.setEmail("test@test.com");
user.setNickname("tester");
```

반면 Builder는 별도의 Builder 객체에 필요한 값을 모은 뒤 `build()` 시점에 객체를 생성한다.

```java
User user = User.builder()
        .email("test@test.com")
        .nickname("tester")
        .build();
```

따라서 객체 생성 코드를 읽기 쉽고, 생성 이후 객체의 상태를 불필요하게 열어놓지 않을 수 있다.

Lombok의 `@Builder`를 사용하면 Builder 코드를 자동으로 생성할 수 있다.

```java
@Builder
private User(String email, String nickname) {
    this.email = email;
    this.nickname = nickname;
}
```

Entity 전체보다 생성자에 `@Builder`를 적용하면 **객체 생성 시 사용할 수 있는 필드를 명확하게 제한할 수도 있다.**

<br>
<hr>

## 7. MapStruct 

<br>

Controller와 Service에서는 Entity를 그대로 반환하기보다 DTO로 변환해서 사용하는 경우가 많다.

직접 변환하면 다음과 같은 코드가 반복될 수 있다.

```java
return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname()
);
```

MapStruct는 이런 반복적인 객체 변환 코드를 자동으로 생성해주는 도구이다.

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponse toResponse(User user);
}
```
`componentModel = "spring"`을 사용하면 생성된 Mapper를 Spring Bean으로 등록하여 주입받아 사용할 수 있다.

```java
private final UserMapper userMapper;

return userMapper.toResponse(user);
```

필드 이름이 다른 경우 `@Mapping`을 사용할 수 있다.

```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
```

```aiignore
Product.category.id → ProductResponse.categoryId
```

즉 Entity와 DTO의 구조가 달라도 원하는 필드를 지정하여 매핑할 수 있다.

<br>
<hr>

## 8. 전체 흐름

<br>

이번에 배운 내용을 하나의 회원가입 API에 적용하면 다음과 같이 볼 수 있다.

```aiignore
Client
  ↓
Controller
  │ @Valid
  ↓
Request DTO
  ↓
Service
  │ 비즈니스 로직
  │ @Transactional
  ↓
Repository
  ↓
Database
```

```aiignore
Service
  ↓
MapStruct
  ↓
Response DTO
  ↓
Controller 
  ↓
Client
```

각 계층이 자신의 역할을 나누어 담당하는 것이 중요하다.

<br>
<hr>

## 마치며

<br>

이번 학습에서 가장 중요했던 부분은 단순히 어노테이션 사용법을 외우는 것이 아니라 **각 기능이 어느 계층에서 어떤 책임을 가져야 하는지 이해하는 것**이었다.

```aiignore
Validation → 잘못된 입력을 애플리케이션 내부로 전달하지 않는다.

Lombok → 반복 코드를 줄이되 필요한 기능만 선택해서 사용한다.

Service → Repository 호출을 조합하여 하나의 비즈니스 행위를 만든다.

@Transactional → 하나의 비즈니스 행위를 하나의 트랜잭션으로 관리한다.

Builder → 객체의 생성 과정을 명확하게 표현한다.

MapStruct → Entity와 DTO 사이의 반복적인 변환 코드를 줄인다.
```

특히 Service는 단순히 Repository 메서드를 대신 호출하는 곳이 아니라 **여러 작업을 하나의 의미 있는 비즈니스 행위로 묶은 계층**이라는 점을 기억하자!

<br>

