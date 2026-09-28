<br>

# Week 2. Spring REST API & JPA Advanced

<br>

2주차에는 Spring REST API의 요청 및 응답 처리부터 Validation, 공통 예외 처리, Service 계층의 역할, 트랜잭션과 JPA 영속성 테스트, QueryDSL까지 학습했다.

1주차가 Spring 애플리케이션의 기본 구조와 JPA의 사용법을 이해하는 과정이었다면, 2주차는 각 계층의 책임을 명확하게 분리하고 데이터의 정합성과 조회 효율을 관리하는 방법을 이해하는 과정이었다.

전체적인 흐름은 다음과 같다.

```aiignore
Client
  ↓  HTTP Request
Controller
  │  요청 데이터 바인딩
  │  @Valid 검증
  ↓  
Service
  │  비즈니스 로직
  │  @Transactional
  ↓  
Repository
  │  JPA / QueryDSL
  ↓  
Persistence Context
  ↓  
Database
  ↓  
Response DTO
  ↓  
ApiResponse / ResponseEntity
  ↓  
Client
```

정상적인 요청뿐만 아니라 예외가 발생했을 때의 처리 흐름과 데이터베이스 작업이 실패했을 때의 Rollback까지 함께 이해하는 것이 중요하다.

<br>
<hr>

## 1. Controller와 HTTP 요청 처리

<br>

Controller는 클라이언트의 HTTP 요청을 받아 필요한 데이터를 전달받고, Service를 호출하여 결과를 응답하는 계층이다.

Spring MVC에서는 요청 데이터의 위치에 따라 서로 다른 어노테이션을 사용한다.

<table>
    <thead>
        <th>어노테이션</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>@PathVariable</td>
            <td>URL 경로의 값</td>
        </tr>
        <tr>
            <td>@RequestParam</td>
            <td>Query String의 값</td>
        </tr>
        <tr>
            <td>@ModelAttribute</td>
            <td>요청 파라미터를 객체에 바인딩</td>
        </tr>
        <tr>
            <td>@RequestBody</td>
            <td>HTTP Body를 Java 객체로 변환</td>
        </tr>
    </tbody>
</table>

예를 들어 다음과 같이 사용할 수 있다.

```java
// [GET] /products/id
@GetMapping("/{id}")
public ProductResponse getProduct(@PathVariable Long id) {
    return productService.getProduct(id);
}

// [GET] /products?name=keyboard
@GetMapping
public List<ProductResponse> getProducts(@RequestParam String name) {
    return productService.getProducts(name);
}
```

핵심은 Controller에서 비즈니스 로직을 직접 처리하지 않고 요청과 응답을 담당할 수 있도록 역할을 분리하는 것이다.

<br>
<hr>

## 2. Validation과 DTO

<br>

Validation은 클라이언트가 전달한 데이터가 애플리케이션에서 요구하는 규칙을 만족하는지 검증하는 과정이다.

Request DTO에 검증 조건을 선언하고 Controller에서 @Valid를 사용한다.

```java
public record ProductCreateRequest (
        @NotBlank String name, @NotNull @Positive BigDecimal price
) {
}

@PostMapping
public ProductResponse create(
        @Valid @RequestBody ProductCreateRequest request
) {
    return productService.create(request);
}
```

대표적인 검증 어노테이션은 다음과 같다.

<table>
    <thead>
        <th>어노테이션</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>@NotNull</td>
            <td>null 허용 X</td>
        </tr>
        <tr>
            <td>@NotBlank</td>
            <td>null, 빈 문자열, 공백 허용 X</td>
        </tr>
        <tr>
            <td>@NotEmpty</td>
            <td>null, 빈 문자열 및 빈 컬렉션 허용 X</td>
        </tr>
        <tr>
            <td>@Size</td>
            <td>문자열 및 컬렉션 크기 제한</td>
        </tr>
        <tr>
            <td>@Positive</td>
            <td>양수 검증</td>
        </tr>
        <tr>
            <td>@Email</td>
            <td>이메일 형식 검증</td>
        </tr>
    </tbody>
</table>

요청 데이터의 형식 검증은 DTO에서 처리하고, 이메일 중복이나 재고 부족처럼 실제 비즈니스 규칙과 관련된 검증은 Service 또는 Domain에서 처리한다.

<br>
<hr>

## 3. 공통 응답과 예외 처리

<br>

API마다 서로 다른 응답 형식을 사용하면 클라이언트가 응답을 처리하기 어려워진다.

이를 해결하기 위해 공통 응답 구조와 예외 처리 구조를 사용할 수 있다.

<br>

### · ResponseEntity와 ApiResponse

두 클래스는 역할이 다르다.

```aiignore
ResponseEntity
    ├── HTTP Status
    ├── Header
    └── Body
         └── ApiResponse<T>
                ├── error
                └── data
```

- ResponseEntity : HTTP 응답 전체를 표현한다.
- ApiResponse : 서비스에서 사용하는 공통 응답 Body를 정의한다.

### · GlobalExceptionHandler

Controller마다 동일한 try-catch를 작성하는 대신 @RestControllerAdvice를 사용하면 여러 Controller의 예외를 공통으로 처리할 수 있다.

```aiignore
Controller → Service → throw DomainException → GlobalExceptionHandler 

→ ApiResponse.fail() → HTTP Response
```

각 클래스의 책임은 다음과 같다.

<table>
    <thead>
        <th>구성 요소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>ErrorCode</td>
            <td>에러의 종류와 코드 정의</td>
        </tr>
        <tr>
            <td>DomainException</td>
            <td>비즈니스 예외 표현</td>
        </tr>
        <tr>
            <td>GlobalExceptionHandler</td>
            <td>공통 예외 처리</td>
        </tr>
        <tr>
            <td>ApiResponse</td>
            <td>응답 Body 통일</td>
        </tr>
        <tr>
            <td>ResponseEntity</td>
            <td>HTTP 상태 및 응답 전달</td>
        </tr>
    </tbody>
</table>

즉, 예외를 발생시키는 책임과 예외를 HTTP 응답으로 변환하는 책임을 분리하는 것이 중요하다.

<br>
<hr>

## 4. Service 계층과 비즈니스 로직

<br>

Service는 단순히 Repository 메서드를 호출하는 계층이 아니다.

여러 데이터 접근과 검증 작업을 조합하여 하나의 의미 있는 비즈니스 행위를 수행한다.

예를 들어 상품 주문이라는 기능은 다음과 같이 구성될 수 있다.

```aiignore
주문 요청 → 상품 조회 → 재고 확인 → 재고 차감 → 주문 생성 → 주문 저장
```

이러한 작업들을 하나의 트랜잭션으로 관리하는 것이 일반적이다.

또한 Service끼리 무분별하게 참조하면 순환 의존성이 발생할 수 있으므로 각 도메인의 책임을 명확하게 구분해야 한다.

<br>
<hr>

## 5. Builder와 MapStruct

<br>

### · Builder

Builder는 객체 생성에 필요한 값을 명시적으로 전달할 수 있도록 도와주는 패턴이다.

```java
Product product = Product.builder()
        .name("Keyboard")
        .price(new BigDecimal("50000"))
        .build();
```

Entity 전체에 Setter를 열어두기보다 생성 시점에 필요한 값을 전달하고, 이후 상태 변경은 의미 있는 메서드를 통해 수행하는 방식이 좋다.


### · MapStruct

MapStruct는 Entity와 DTO 사이의 반복적인 변환 코드를 자동으로 생성하는 라이브러리이다.

```java
@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    ProductResponse toResponse(Product product);
}
```

```aiignore
Entity → MapStruct → Response DTO
```

componentModel = "spring"을 사용하면 생성된 Mapper 구현체를 Spring Bean으로 등록하여 사용할 수 있다.

<br>
<hr>

## 6. Transaction과 ACID

<br>

트랜잭션은 여러 데이터베이스 작업을 하나의 논리적인 작업 단위로 묶는 기술이다.

트랜잭션은 ACID 특성을 가진다.

<table>
    <thead>
        <th>특성</th>
        <th>의미</th>
    </thead>
    <tbody>
        <tr>
            <td>Atomicity</td>
            <td>원자성: 모든 작업의 성공 또는 취소</td>
        </tr>
        <tr>
            <td>Consistency</td>
            <td>일관성: 데이터베이스 규칙 유지</td>
        </tr>
        <tr>
            <td>Isolation</td>
            <td>격리성: 동시 트랜잭션 간 간섭 제어</td>
        </tr>
        <tr>
            <td>Durability</td>
            <td>지속성: Commit된 데이터 보존</td>
        </tr>
    </tbody>
</table>

핵심은 All or Nothing이다.

```aiignore
주문 저장 → 재고 차감 → 결제 정보 저장 → 성공 → Commit
                                    중간 실패 → Rollback
```

하나의 비즈니스 작업에 포함된 데이터베이스 변경이 부분적으로만 반영되지 않도록 관리해야 한다.

<br>
<hr>

## 7. @Transactional과 Rollback

<br>

Spring에서는 @Transactional을 통해 선언적으로 트랜잭션을 관리할 수 있다.

```java
@Transactional
public void order(Long productId, int quantity) {
    
    // 주문 저장
    // 재고 차감
    // 결제 정보 저장
}
```

기본 동작은 다음과 같다.

- 정상 종료 시 Commit한다.
- RuntimeException 또는 Error 발생 시 Rollback한다.
- Checked Exception은 기본적으로 Rollback 대상이 아니다.
- 기본 전파 속성은 REQUIRED이다.

조회 전용 메서드에는 다음과 같이 사용할 수 있다.

```java
@Transactional(readOnly = true)
public ProductResponse getProduct(Long id) {
    // 조회 로직
}
```

`readOnly = true`는 조회 전용 트랜잭션임을 표현하고 일부 최적화에 활용될 수 있다.

단, Reader DB로 자동 연결되거나 쓰기 작업이 완전히 차단되는 것은 아니다.

<br>

### · 주의할 점

Spring의 일반적인 프록시 기반 트랜잭션에서는 같은 클래스 내부의 메서드를 직접 호출하면 트랜잭션 프록시를 거치지 않는다.

따라서 @Transactional을 선언했다고 해서 모든 호출에서 항상 트랜잭션이 새롭게 적용되는 것은 아니다.

<br>
<hr>

## 8. 영속성 컨텍스트와 Entity 생명주기

영속성 컨텍스트는 JPA가 Entity를 관리하는 논리적인 공간이다.

EntityManager는 영속성 컨텍스트를 통해 Entity의 상태와 변경 사항을 관리한다.

<br>

### · Entity의 4가지 상태

<table>
    <thead>
        <th>상태</th>
        <th>의미</th>
    </thead>
    <tbody>
        <tr>
            <td>비영속</td>
            <td>영속성 컨텍스트에서 관리되지 않는 상태</td>
        </tr>
         <tr>
            <td>영속</td>
            <td>영속성 컨텍스트에서 관리하는 상태</td>
        </tr>
         <tr>
            <td>준영속</td>
            <td>관리되던 Entity가 분리된 상태</td>
        </tr>
         <tr>
            <td>삭제</td>
            <td>삭제 대상으로 등록된 상태</td>
        </tr>
    </tbody>
</table>

영속성 컨텍스트의 주요 기능은 다음과 같다.

```aiignore
1. 1차 캐시
2. 동일성 보장
3. 쓰기 지연
4. 변경 감지 (Dirty Checking)
```

예를 들어 동일한 영속성 컨텍스트에서 같은 ID의 Entity를 조회하면 관리 중인 동일한 객체를 반환한다.

```java
Product a = entityManager.find(Product.class, 1L);
Product b = entityManager.find(Product.class, 1L);

System.out.println(a == b); // true
```

즉, JPA는 단순히 SQL을 실행하는 도구가 아니라 Entity의 상태를 지속적으로 관리한다.

<br>
<hr>

## 9. Dirty Checking과 Flush

<br>

## · Dirty Checking

Dirty Checking은 영속 상태의 Entity에 발생한 변경 사항을 JPA가 자동으로 감지하는 기능이다.

```java
@Transactional
public void changePrice(Long id, BigDecimal price) {
    
    Product product = productRepository.findById(id).orElseThrow();
    
    product.changePrice(price);
}
```

영속 상태의 Entity를 수정하면 트랜잭션이 정상적으로 Commit되는 과정에서 변경 사항이 데이터베이스에 반영된다.

별도의 save() 호출이 반드시 필요한 것은 아니다.

## · Flush와 Commit

둘은 같은 개념이 아니다.

<table>
    <thead>
        <th>구분</th>
        <th>Flush</th>
        <th>Commit</th>
    </thead>
    <tbody>
        <tr>
            <td>역할</td>
            <td>영속성 컨텍스트의 변경 내용을 DB에 동기화</td>
            <td>트랜잭션 최종 확정</td>
        </tr>
        <tr>
            <td>트랜잭션 종료</td>
            <td>X</td>
            <td>O</td>
        </tr>
        <tr>
            <td>Rollback</td>
            <td>가능</td>
            <td>확정된 트랜잭션은 일반적으로 불가능</td>
        </tr>
    </tbody>
</table>

전체 흐름은 다음과 같다.

```aiignore
Entity 조회 → 영속성 컨텍스트에서 관리 → Entity 값 변경 → Dirty Checking

→ Flush → UPDATE SQL 실행 → Commit
```

Flush가 발생했다고 해서 Commit된 것은 아니다.

또한 Flush는 영속성 컨텍스트를 비우는 작업이 아니며, 영속성 컨텍스트를 비우는 메서드는 clear()이다.

<br>
<hr>

## 10. QueryDSL과 동적 쿼리

<br>

QueryDSL은 문자열 대신 Java 코드를 사용하여 타입 안전한 쿼리를 작성할 수 있도록 도와주는 라이브러리이다.

JPQL에 비해 컴파일 시점에 많은 오류를 확인할 수 있으며, 동적 검색 조건을 구성하기 편리하다.

<br>

### · Q클래스

Entity를 기반으로 쿼리 전용 클래스가 자동 생성된다.

```aiignore
Product.java
    ↓ Annotation Processor
QProduct.java
```

### · 동적 쿼리

검색 조건이 있을 때만 WHERE 절에 추가할 수 있다.

```java
.where(
        nameContains(name),
        priceGoe(minPrice),
        priceLoe(maxPrice)
)
```

조건을 BooleanExpression으로 분리하면 재사용하기 쉽다.

주요 메서드는 다음과 같다.

<table>
    <thead>
        <th>메서드</th>
        <th>의미</th>
    </thead>
    <tbody>
        <tr>
            <td>eq()</td>
            <td>일치</td>
        </tr>
        <tr>
            <td>goe()</td>
            <td>이상</td>
        </tr>
        <tr>
            <td>loe()</td>
            <td>이하</td>
        </tr>
        <tr>
            <td>contains()</td>
            <td>문자열 포함</td>
        </tr>
        <tr>
            <td>in()</td>
            <td>여러 값 중 하나</td>
        </tr>
    </tbody>
</table>

### · DTO Projection

Entity 전체가 아닌 필요한 컬럼만 조회하여 DTO에 직접 담을 수 있다.

```java
.select(new QProductDto(
        product.name,
        product.price
))
```

<br>
<hr>

## 2주차 핵심 흐름

<br>

이번 주에 배운 내용을 하나의 API 요청으로 연결하면 다음과 같다.

```aiignore
1. Client가 HTTP 요청을 보낸다.
                ↓
2. Controller가 요청 데이터를 받는다.
                ↓
3. @Valid를 통해 Request DTO를 검증한다.
                ↓
4. Service가 비즈니스 로직을 수행한다.
                ↓
5. @Transactional로 작업 단위를 관리한다.
                ↓
6. Repository가 JPA / QueryDSL로 데이터를 조회한다.
                ↓
7. 영속성 컨텍스트가 Entity 상태를 관리한다.
                ↓
8. Entity 변경 사항을 Dirty Checking으로 감지한다.
                ↓
9. Flush를 통해 SQL을 실행하고 Commit한다.
                ↓
10. Entity를 Response DTO로 변환한다.
                ↓
11. ApiResponse로 응답 형식을 통일한다.
                ↓
12. Client에게 HTTP Response를 반환한다.
```

트랜잭션 실행 중 예외가 발생하여 Rollback 조건을 만족하면 해당 트랜잭션의 변경 작업은 취소된다.

<br>
<hr>

## 마치며

<br>

2주차에서 가장 중요한 것은 개별 어노테이션의 사용법을 외우는 것보다 **각 계층의 책임과 데이터의 상태 변화가 어떻게 연결되는지 이해하는 것**이다.

```aiignore
Controller  → HTTP 요청과 응답 처리    

Validation  → 입력 데이터 검증

Service  → 비즈니스 로직 수행

ExceptionHandler  → 공통 예외 처리

Transaction  → 데이터 작업의 정합성 관리

Persistence  → Entity의 상태 관리

Dirty Checking  → Entity 변경 사항 감지

QueryDSL  → 타입 안전한 데이터 조회
```

결국 2주차에 배운 내용들은 모두 다음과 같은 질문으로 연결된다.

**"Spring에서 요청을 안전하게 처리하고, 데이터의 정합성을 유지하면서 효율적으로 조회하려면 어떻게 설계해야 하는가?"**

1주차에서 Spring과 JPA의 기본 구조를 학습했으며, 2주차에서는 이를 실제 서비스의 비즈니스 로직과 데이터 처리에 적용하는 방법을 학습했다.

<br>