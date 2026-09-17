<br>

# Day 3. Spring Data JPA - 의존성 주입과 연관관계 

<br>

## 1. 의존성 주입 (DI)

<br>

의존성 주입은 Day1에 간단하게 정리했지만, 수업을 통해 다시 만나게 되어 중요한 개념이니 또 정리를 해보고자 한다.

Spring에서는 객체가 필요한 다른 객체를 직접 생성하지 않고, Spring Container가 만들어 둔 Bean을 주입받아 사용한다.

예를 들어 `UserService`에서 `UserRepository`가 필요하다고 해보자.

<br>

### 필드 주입

```java
@Autowired
private UserRepository userRepository;
```

Spring이 `UserService` 객체를 생성한 뒤 `userRepository` 필드에 `UserRepository` Bean을 넣어준다.

코드가 간단하지만 다음과 같은 단점이 있다.

- `final`을 사용할 수 없다.
- 객체가 어떤 의존성을 필요로 하는지 생성자만 보고 알기 어렵다.
- 순수 Java 테스트에서 의존성을 직접 넣기 불편하다.

<br>

### 생성자 주입

```java
private final UserRepository userRepository;

public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

객체가 생성되는 순간 필요한 의존성을 전달받는다.

필드 주입과 생성자 주입 모두 **Spring이 `UserRepository` Bean을 찾아 `UserService`에 넣어준다는 목적은 같다.**

하지만 주입되는 시점과 방식이 다르다.

필드 주입은 객체가 만들어진 **이후** 값을 넣고, 생성자 주입은 객체가 만들어지는 **과정에서** 값을 전달한다.

<br>
<hr>

## 2. 생성자 주입에서 `final`을 사용하는 이유

<br>

```java
private final UserRepository userRepository;
```

`final`이 붙은 필드는 생성자에서 반드시 초기화되어야 하고, 한 번 값이 들어간 이후 다른 객체로 변경할 수 없다.

따라서 `UserService`가 생성될 때부터 반드시 `UserRepository`를 가지고 있도록 보장할 수 있다.

반면 필드 주입은 다음과 같이 객체가 생성된 이후 Spring이 값을 넣는다.

```java
@Autowired
private UserRepository userRepository;
```

`final` 필드는 생성 시점에 값이 결정되어야 하기 때문에 이런 필드 주입 방식과 맞지 않는다.

그래서 실무에서는 일반적으로 **생성자 주입 + final** 조합을 사용한다.

```java
private final UserRepository userRepository;

@Autowired
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

그리고 클래스에 생성자가 하나뿐이라면 Spring에서는 `@Autowired`를 생략할 수 있다.

```java
private final UserRepository userRepository;

public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

두 코드는 동일하게 생성자 주입으로 동작한다.

<br>
<hr>

## 3. `@RequiredArgsConstructor`

Lombok의 `@RequiredArgsConstructor`를 사용하면 필요한 필드를 매개변수로 받는 생성자를 자동으로 만들어준다.

```java
@Service
@RequiredArgsConstructor 
public class UserService {
    
    private final UserRepository userRepository;
}
```

실제로는 다음과 비슷한 생성자가 만들어진다.

```java
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

따라서 Spring 프로젝트에서는 다음 조합을 자주 볼 수 있다.

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
}
```

`final` 필드가 늘어나면 생성자에도 자동으로 추가되기 때문에 코드도 간결해진다.

<hr>
<br>

## 4. Entity에서 전역 `@Setter`를 지양하는 이유

<br>

Entity에 다음과 같이 `@Setter`를 사용하면 모든 필드를 어디에서든 변경할 수 있다.

```java
@Setter
@Entity
public class User {
    ...
}
```

예를 들어 이름에 대한 규칙이 남아 있음에도 다음과 같은 코드가 작성될 수 있다.

```java
user.setName("");
```

Entity 내부에서 값 변경을 관리할 수 없기 때문에 잘못된 상태가 만들어질 가능성이 높아진다.

따라서 필요한 값만 변경할 수 있도록 의미가 드러나는 메서드를 만드는 방법을 많이 사용한다.

```java
public void changeName(String name) {
    
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
    }
    
    this.name = name;
}
```

사용하는 코드도 조금 더 명확해진다.

```java
user.changeName("홍길동");
```

`setName()`보다 **무엇을 하기 위한 코드인지 의도가 명확하고 검증 로직도 Entity 내부에서 관리할 수 있다.**

<hr>
<br>

## 4-2. Entity 기본 생성자와 @NoArgsConstructor

<br>

`JPA Entity`는 `기본 생성자(매개변수가 없는 생성자)`가 필요하다.

생성자를 하나도 작성하지 않았다면 Java가 자동으로 기본 생성자를 만들어 주기 때문에 `@NoArgsConstructor`가 없어도 된다.

```java
@Entity
public class User {
    
    @Id
    private Long id;
    
    private String name;
}
```

하지만 생성자를 하나라도 직접 작성하면 Java가 기본 생성자를 자동으로 만들어주지 않는다.

```java
public User(String name) {
    this.name = name;
}
```
이 경우 JPA가 사용할 기본 생성자를 만들어주기 위해 Lombok의 `@NoArgsConstructor`를 사용할 수 있다.

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

실무에서는 기본 생성자를 외부에서 함부로 호출하지 못하도록 다음과 같이 `protected`로 제한하는 경우가 많다.

정리하면,

```aiignore
* 생성자 없음
→ Java가 기본 생성자를 자동 생성
→ @NoArgsConstructor 없어도 됨

* 생성자를 직접 작성함
→ 기본 생성자가 자동 생성되지 않음
→ JPA를 위해 @NoArgsConstructor 추가
```

JPA Entity의 기본 생성자는 `public` 또는 `protected` 접근 제어자를 사용해야 한다.

<hr>
<br>

## 5. Spring Data JPA Repository

<br>

`JpaRepository`를 사용하면 기본적인 CRUD 메서드를 사용할 수 있다.

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

대표적으로 다음과 같은 메서드가 이미 제공된다.

```aiignore
findAll()
findById()
save()
deleteById()
existsById()
```

따라서 위 메서드는 따로 선언하지 않아도 된다.

Spring Data JPA는 메서드 이름을 분석해서 Query를 생성할 수도 있다.

```java
Optional<User> findByEmail(String email);
```

위 메서드는 대략 다음 조건의 Query로 변환된다.

```aiignore
WHERE email = ?
```
따라서 단순한 조건이라면 `@Query`를 작성할 필요가 없다.

직접 만든 쿼리가 필요하거나 메서드 이름만으로 표현하기 어려운 경우 `@Query`를 사용할 수 있다.

<hr>
<br>

## JPA 연관관계
## 6. N:1 관계 - `@ManyToOne`

예를 들어 한 명의 User가 여러 개의 Order를 만들 수 있다.

관계는 다음과 같다.

```aiignore
User 1 : N Order
```

Order 입장에서 보면 여러 Order가 하나의 User를 바라보기 때문에 `@ManyToOne`을 사용한다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

실무에서는 연관관계가 필요한 경우 `@ManyToOne`을 중심으로 설계하는 경우가 많다.

`@ManyToOne`의 JPA 기본 FetchType은 `EAGER`이지만, 불필요한 조회를 줄이기 위해 명시적으로 `LAZY`를 사용하는 경우가 많다.

<hr>
<br>

## 7. `@OneToMany`를 조심해서 사용하는 이유 

<br>

User에서 Order 목록을 가지고 있다면 다음과 같이 표현할 수 있다.

```java
@OneToMany(mappedBy = "user")
private List<Order> orders = new ArrayList<>();
```

그리고 다음처럼 사용할 수도 있다.

```java
User user = userRepository.findById(userId)
        .orElseThrow();

return user.getOrders();
```

`@OneToMany` 자체가 잘못된 것은 아니다.

다만 부모 Entity가 자식 Entity 목록을 계속 가지고 있으면 Entity가 복잡해지고, Lazy Loading이나 N+1 문제를 신경 써야 하며, 컬렉션이 커질수록 관리하기 어려워질 수 있다.

또한 양방향 관계를 그대로 JSON으로 반환하면 서로를 계속 참조하면서 **무한 순환 참조**가 발생할 수도 있다.

따라서 단순히 특정 User의 Order 목록이 필요한 경우에는 OrderRepository에서 직접 조회하는 방법도 많이 사용한다.

```java
List<Order> findAllByUser_Id(Long userId);
```

Service에서는 다음처럼 사용할 수 있다.

```java
return orderRepository.findAllByUser_Id(userId);
```

<hr>
<br>

## 8. Repository 메서드 `_`의 의미

```java
findAllByUser_Id(userId)
```

여기서 `_`가 SQL의 `.`으로 변환되는 것은 아니다.

Spring Data JPA에서 `_`는 **객체의 프로퍼티 경계를 명확하게 표현하기 위한 구분자**다.

즉,

```aiignore
User_Id 는 user → id
```
라는 객체 탐색을 의미한다.

쉽게 생각하면 다음 조건을 표현한다고 볼 수 있다.

```aiignore
Order.user.id = userId;
```

따라서 SQL의 점(`.`)이라기 보다는 **Entity의 연관된 객체 내부 필드로 이동한다는 의미**로 이해하는 것이 좋다.

<hr>
<br>

## 9. N:N 관계와 `@ManyToMany`

예를 들어 User와 Product가 있다고 해보자.

한 User가 여러 Product를 구매할 수 있고, 하나의 Product도 여러 User에게 구매될 수 있다.

```aiignore
User N : N Product
```

JPA에서는 `@ManyToMany`로 표현할 수 있지만 실무에서는 중간 Entity를 만들어 사용하는 경우가 많다.

```aiignore
User → UserProduct → Product
```

중간 Entity에서는 각각 `@ManyToOne`으로 연결한다.

```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;

@ManyToOne(fetch = FetchType.LAZY)
private Product product;
```

이렇게 하면 중간 테이블에 추가 정보도 저장할 수 있다.

예를 들어 다음과 같은 값이다.

```aiignore
구매 날짜
수량
가격
상태
```

`@ManyToMany`를 직접 사용하면 이런 관계 자체에 대한 정보를 추가하기 어렵다.

따라서 실제 서비스에서는 N:N 관계를 중간 Entity로 풀어서 **양쪽을 각각 N:1 관계로 만드는 방법을 많이 사용한다.**

<hr>
<br>

## 마치며

<br>

```java
*필드 주입

@Autowired
private UserRepository userRepository;


* 생성자 주입

private final UserRepository userRepository;

public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

                    ↓

* Lombok 사용

@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
}
```

세 방법 모두 Spring Bean을 주입받을 수 있지만, 보통은 **생성자 주입 + final**을 사용한다.

JPA 연관관계에서는 모든 관계를 Entity에 양방향으로 넣기보다 필요한 방향만 연결하고, 목록 조회가 필요하다면 Repository Query를 이용하는 방법도 고려한다.

또한 N 관계는 `@ManyToMany`로 바로 연결하기보다 중간 Entity를 두어 관계 자체를 하나의 객체로 관리하는 방법이 실무에서 유연하다.

<br>

