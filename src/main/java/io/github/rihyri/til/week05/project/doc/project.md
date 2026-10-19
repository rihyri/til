<br>

# Week05 - Project. Spring Security와 Redis 기반 장바구니 구현

<br>

## 1. 장바구니 기능 설계

<br>

Spring Security의 세션 인증과 Redis 캐싱을 활용하여 로그인한 사용자의 장바구니 CRUD 기능을 구현한다.

이번 구현에서는 장바구니의 원본 데이터를 RDBMS에 저장하고, Redis를 인증 세션과 조회 캐시 저장소로 사용한다.

<table>
    <thead>
        <th>저장소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>RDBMS</td>
            <td>사용자, 상품, 장바구니 원본 데이터 저장</td>
        </tr>
        <tr>
            <td>Redis Session</td>
            <td>로그인한 사용자의 인증 세션 관리</td>
        </tr>
        <tr>
            <td>Redis Cache</td>
            <td>장바구니 조회 결과를 임시 저장</td>
        </tr>
    </tbody>
</table>

장바구니 조회에는 **Cache-Aside**, 추가·수정·삭제는 **Write-around + Cache Invalidation** 전략을 적용한다.

<br>
<hr>

## 2. CartItem 엔티티 설계

<br>

### 2-1. Cart와 CartItem

- `Cart` : 사용자가 소유한 장바구니 자체
- `CartItem` : 장바구니에 담긴 개별 상품 항목

사용자마다 장바구니가 하나씩 존재하더라도 반드시 Cart 생성을 해야 하는 것은 아니다.

이번 구현에서는 별도의 Cart 엔티티 없이 CartItem에 `userId`, `productId`, `quantity`를 저장한다.

하나의 사용자가 여러 CartItem을 가질 수 있으므로 다음과 같은 연관관계를 사용한다.

<table>
    <thead>
        <th>관계</th>
        <th>연관관계</th>
    </thead>
    <tbody>
        <tr>
            <td>User → CartItem</td>
            <td>1</td>
        </tr>
        <tr>
            <td>Product → CartItem</td>
            <td>1</td>
        </tr>
        <tr>
            <td>CartItem → User</td>
            <td>N:1</td>
        </tr>
        <tr>
            <td>CartItem → Product</td>
            <td>N:1</td>
        </tr>
    </tbody>
</table>

CartItem에서 `@ManyToOne`을 사용하여 User와 Product를 참조한다.


<br>


### 2-2. 복합 UNIQUE 제약 조건

동일 사용자가 같은 상품을 여러 행으로 저장하지 않도록 `(user_id, product_id)`에 복합 UNIQUE 제약 조건을 적용한다.

```java
@Table(
        name = "carts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_product",
                columnNames = {"user_id", "product_id"}
        )
)
```

이미 장바구니에 존재하는 상품을 추가할 경우 새로운 CartItem을 생성하지 않고 기존 수량을 증가시킨다.

단, UNIQUE 제약 조건만으로 동시 요청에 따른 수량 변경 충돌까지는 방지할 수는 없다.

<br>
<hr>

## 3. EntityGraph와 상품 조회

<br>

장바구니 목록에는 상품 ID뿐 아니라 상품명, 가격 등의 정보가 필요하다.

CartItem과 Product는 LAZY 연관관계이므로, 장바구니 목록을 조회한 후 각 상품에 접근하면 추가 SELECT가 발생할 수 있다.

이를 해결하기 위해 Repository에서 `@EntityGraph`를 사용한다.

```java
@EntityGraph(attributePaths = "product")
List<CartItem> findByUser_IdOrderByIdAsc(Long userId);
```

`@EntityGraph`는 해당 조회에서 함께 로딩할 연관 엔티티를 지정한다.

Hibernate에서는 이러한 단일 연관관계를 일반적으로 JOIN으로 가져오므로 N+1 조회 문제를 줄일 수 있다.

기본 LAZY 설정을 변경하는 것이 아니라, 특정 Repository 메서드의 조회 계획을 지정한다는 점이 중요하다.

<br>
<hr>

## 4. Redis 캐싱 전략

<br>

### 4-1. Cache-Aside (Read)

애플리케이션이 Redis를 먼저 조회하고, 캐시가 없을 때 DB를 조회하는 전략이다.

#### Cache-Hit
1. Redis에서 캐시 키를 조회한다.
2. 캐시 데이터가 존재하면 DB를 조회하지 않는다.
3. Redis에 저장된 데이터를 반환한다.


#### Cache-Miss
1. Redis에서 캐시 키를 조회한다.
2. 캐시 데이터가 없다면 DB에서 조회한다.
3. DB 조회 결과를 Redis에 저장한다.
4. 조회 결과를 반환한다.

이번 구현에서는 `@Cacheable`을 활용한다.

```java
@Cacheable(
        cacheNames = "cartItems",
        key = "#userId"
)
```

사용자 ID가 1이라면 기본 키 접두어 설정에서 `cartItems::1`이라는 캐시 키를 사용한다.

Cache Hit인 경우 메서드 본문 자체가 실행되지 않으므로 DB 조회 로그도 출력되지 않는다.

<br>

### 4-2. Write-around + Cache Invalidation (Write)

장바구니 추가·수정·삭제는 DB에 먼저 반영하고 Redis에 변경된 데이터를 직접 저장하지 않는다.

대신 기존 캐시를 무효화한다.

1. DB에서 장바구니 데이터를 변경한다.
2. DB 트랜잭션을 커밋한다.
3. 해당 사용자의 Redis 캐시를 삭제한다.
4. 다음 조회에서 Cache Miss가 발생한다.
5. 최신 db 데이터를 다시 캐싱한다.

Write-around는 쓰기 시 캐시를 직접 갱신하지 않는 방식이며, 이번 구현에서는 캐시 무효화를 함께 사용한다.

<br>
<hr>

5. RedisCacheConfig와 TTL

Spring Cache를 Redis와 연결하기 위해 `RedisCacheManager`를 사용한다.

<br>

### 5-1. 주요 구성 요소

<table>
    <thead>
        <th>구성 요소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>@EnableCaching</td>
            <td>Spring Cache 어노테이션 처리 활성화</td>
        </tr>
        <tr>
            <td>RedisConnectionFactory</td>
            <td>Redis 서버 연결 제공</td>
        </tr>
        <tr>
            <td>RedisCacheManager</td>
            <td>Redis 기반 캐시 조회·저장·수정·삭제 관리</td>
        </tr>
        <tr>
            <td>RedisCacheConfiguration</td>
            <td>TTL, 직렬화 등 캐시 설정</td>
        </tr>
        <tr>
            <td>@Cacheable</td>
            <td>캐시 조회 및 Cache Miss 시 결과 저장</td>
        </tr>
    </tbody>
</table>

```java
RedisCacheConfiguration config =
    RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues();
```

<br>

### 5-2. TTL (Time To Live)

TTL은 캐싱 데이터가 유지되는 시간이다.

이번에는 장바구니 캐시의 TTL을 5분으로 설정했다.

- 캐시가 생성되면 만료 시간이 설정된다.
- 기본 TTL 방식에서는 Cache Hit이 발생해도 만료 시간이 자동으로 연장되지 않는다.
- TTL이 지나면 다음 조회에서 Cache Miss가 발생할 수 있다.
- Redis가 만료를 관리하므로 별도의 스케줄러를 구현할 필요는 없다.

Spring Session의 로그인 세션 TTL과 장바구니 캐시 TTL은 서로 독립적이다.

<br>

### 5-3. Serializable

Redis에 Java 객체를 저장하려면 직렬화 과정이 필요하다.

이번 예제에는 기본 JDK 직렬화를 사용하므로 캐시에 저장할 DTO에 serializable을 구현한다.

Entity를 직접 캐싱하기보다는 필요한 정보만 담은 Response DTO를 캐싱하여 JPA 연관관계 및 직렬화 문제를 줄인다.

<br>
<hr>

## 6. 트랜잭션과 캐시 무효화 이벤트

<br>

### 6-1. ApplicationEventPublisher

장바구니 데이터가 변경되면 다음과 같이 이벤트를 발행한다.

```java
eventPublisher.publishEvent(
        new CartCacheChangedEvent(userId)
);
```

`CartCacheChangedEvent`는 어떤 사용자의 장바구니 캐시를 무효화해야 하는지 전달하는 이벤트 객체이다.

이벤트 발행 자체가 Redis 캐시를 즉시 삭제하는 것은 아니다.

<br>

### 6-2. TransactionalEventListener

<br>

DB 트랜잭션이 정상적으로 커밋된 이후 캐시를 삭제하기 위해 다음 어노테이션을 사용한다.

```java
@TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT
)
```

주요 실행 시점은 다음과 같다. 

<table>
    <thead>
        <th>Phase</th>
        <th>실행 시점</th>
    </thead>
    <tbody>
        <tr>
            <td>BEFORE_COMMIT</td>
            <td>커밋 직전</td>
        </tr>
        <tr>
            <td>AFTER_COMMIT</td>
            <td>커밋 성공 이후</td>
        </tr>
        <tr>
            <td>AFTER_ROLLBACK</td>
            <td>롤백 이후</td>
        </tr>
        <tr>
            <td>AFTER_COMPLETION</td>
            <td>커밋 또는 롤백 완료 이후</td>
        </tr>
    </tbody>
</table>

이번 구현에서는 `AFTER_COMMIT`을 사용하여 DB 변경이 성공한 경우에만 캐시 삭제를 시도한다.

```java
Cache cache = cacheManager.getCache("cartItems");
cache.evict(event.getUserId());
```

DB 트랜잭션이 롤백된다면 AFTER_COMMIT 리스너는 실행되지 않는다.

다만 DB 커밋과 Redis 캐시 삭제는 하나의 원자적 트랜잭션이 아니므로, Redis 장애나 동시 요청으로 인해 캐시 일관성 문제가 발생할 수 있다.

<br>
<hr>

## 7. Spring Security와 장바구니 연동

<br>

모든 장바구니 API는 로그인한 사용자만 접근할 수 있도록 설정한다.

```java
.requestMatchers("/api/carts", "/api/carts/**")
.authenticated()
```

Controller에서는 `@AuthenticationPrincipal`을 통해 현재 로그인 사용자의 `CustomUserDetails`를 가져온다.

```java
@AuthenticationPrincipal CustomUserDetails userDetails;
```

인증 정보에서 userId를 추출하여 해당 사용자의 장바구니를 조회하거나 변경한다.

클라이언트가 직접 userId를 전달하지 않으므로 다른 사용자의 장바구니에 접근할 수 있는 문제를 줄일 수 있다.

캐시 키 또한 세션 ID가 아닌 userId를 기준으로 생성한다.

따라서 로그아웃 후 재로그인하여 세션 ID가 변경되더라도 기존 장바구니 데이터를 조회할 수 있다.

<br>
<hr>

## 8. 캐싱 검증 방법

Postman과 Redis CLI, Hibernate SQL 로그 등을 이용하여 캐시 동작을 확인할 수 있다. 

<table>
    <thead>
        <th>작업</th>
        <th>기대 동작</th>
    </thead>
    <tbody>
        <tr>
            <td>첫 장바구니 조회</td>
            <td>Cache Miss → DB 조회 → Redis 저장</td>
        </tr>
        <tr>
            <td>같은 장바구니 재조회</td>
            <td>Cache Hit → DB 조회 생략</td>
        </tr>
        <tr>
            <td>상품 추가</td>
            <td>DB 변경 → 기존 캐시 삭제</td>
        </tr>
        <tr>
            <td>수량 수정</td>
            <td>DB UPDATE → 기존 캐시 삭제</td>
        </tr>
        <tr>
            <td>상품 삭제</td>
            <td>DB DELETE → 기존 캐시 삭제</td>
        </tr>
        <tr>
            <td>캐시 TTL 만료</td>
            <td>다음 조회에서 Cache Miss</td>
        </tr>
    </tbody>
</table>

<br>

Redis CLI 명령어:

```aiignore
EXISTS cartItems::1
TTL cartItems::1
TYPE cartItems::1
SCAN 0 MATCH cartItems::* COUNT 100
```

- `EXISTS`: 캐시 키 존재 여부
- `TTL`: 남은 만료 시간
- `TYPE`: Redis 자료형 확인
- `SCAN`: 조건에 맞는 키 탐색

Cache Hit 여부는 Redis 키 존재 여부와 함께 DB SELECT 로그를 비교하여 검증한다.

<br>
<hr>

## 9. 학습 핵심 정리

- CartItem만으로 사용자별 장바구니 구현을 할 수 있다.
- `@EntityGraph`는 연관 데이터를 함께 로딩하여 N+1 문제를 줄이는 데 사용한다.
- Cache-Aside는 캐시가 없을 때만 DB를 조회하여 결과를 캐싱한다.
- Write-around와 캐시 무효화를 결합하면 DB를 원본 저장소로 유지할 수 있다.
- `RedisCacheManager`는 캐시 설정과 Redis 저장소 연결을 담당한다.
- TTL은 로그인 세션과 별도의 장바구니 캐시의 만료 시간을 관리한다.
- `ApplicationEventPublisher`는 데이터 변경 이벤트를 전달한다.
- `@TransactionalEventListener(AFTER_COMMIT)`은 DB 커밋 성공 후 캐시 삭제를 실행하도록 한다.
- 캐시 무효화만으로 모든 동시성 및 데이터 일관성 문제가 해결되는 것은 아니다.

<br>