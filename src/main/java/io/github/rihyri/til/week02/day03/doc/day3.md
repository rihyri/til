<br>

# Day 3. Transaction & Persistence Context & QueryDSL

<br>

## 1. Transaction (트랜잭션)

<br>

### 1-1. 트랜잭션이란?

트랜잭션은 데이터베이스의 여러 작업을 하나의 논리적인 작업 단위로 묶어, 데이터의 무결성과 정합성을 보장하는 기술이다.

여러 작업 중 하나라도 실패하면 전체 작업을 취소(Rollback)하고, 모든 작업이 성공하면 최종 반영(Commit)한다.

예를 들어 상품 주문 시 다음 작업이 필요하다.

1. 주문 정보 저장
2. 상품 재고 차감
3. 결제 정보 저장

결제 정보 저장 중 오류가 발생하면 앞서 수행한 주문 저장과 재고 차감도 함께 취소되어야 한다.


### 1-2. ACID: 트랜잭션의 4가지 특성

<table>
    <thead>
        <th>특성</th>
        <th>의미</th>
    </thead>
    <tbody>
        <tr>
            <td>Atomicity (원자성)</td>
            <td>모든 작업이 성공하거나 모두 취소되어야 한다.</td>
        </tr>
        <tr>
            <td>Consistency (일관성)</td>
            <td>트랜잭션 전후에 데이터베이스의 제약조건과 규칙을 유지해야 한다.</td>
        </tr>
        <tr>
            <td>Isolation (격리성)</td>
            <td>동시에 실행되는 트랜잭션의 간섭을 격리 수준에 따라 제어한다.</td>
        </tr>
        <tr>
            <td>Durability (지속성)</td>
            <td>Commit된 데이터는 장애가 발생하더라도 보존되어야 한다.</td>
        </tr>
    </tbody>
</table>

**핵심: All or Nothing**

### 1-3. @Transactional

Spring에서는 @Transactional을 사용하여 트랜잭션을 선언적으로 관리한다.

```java
@Transactional
public void order(Long productId, int quantity) {
    // 주문 저장
    // 재고 차감
    // 결제 정보 저장
}
```

- 메서드가 정상적으로 종료되면 Commit한다.
- 기본적으로 RuntimeException 또는 Error가 발생하면 Rollback 한다.
- Checked Exception은 기본적으로 Rollback 대상이 아니므로 필요에 따라 rollbackFor을 지정한다.
- 기본 전파 속성은 REQUIRED이다. 기존 트랜잭션이 있으면 참여하고, 없으면 새로 생성한다.

> Spring의 일반적인 프록시 방식에서는 외부에서 호출되는 public 메서드에 적용하는 것이 기본이다. 
> 같은 클래스 내부에서 자신의 메서드를 호출하면 트랜잭션 프록시를 거치지 않는다.

### 1-4. @Transactional(readOnly = true)

조회 전용 트랜잭션임을 나타낸다.

```java
@Transactional(readOnly = true)
public Product getProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow();
}
```

- 조회 작업임을 명시한다.
- JPA/Hibernate의 변경 감지 관련 비용을 줄이는 최적화가 적용될 수 있다.
- 쓰기 작업을 완전히 차단하는 보안 장치는 아니다.
- Write/Reader DB 분리는 별도의 라우팅 설정이 필요하다.

### Writer DB와 Reader DB

- Writer DB : INSERT, UPDATE, DELETE 등을 처리하는 주 데이터베이스
- Reader DB : 주 데이터베이스의 데이터를 복제하여 조회 부하를 분산하는 데이터베이스


```azure
readOnly = true만 작성한다고 자동적으로 Reader DB에 연결되는 것은 아니다.
```

<br>
<hr>

## 2. Persistence Context (영속성 컨텍스트)

### 2-1. 영속성 컨텍스트란?

JPA가 엔티티를 관리하는 논리적인 공간이다.

애플리케이션과 데이터베이스 사이에서 엔티티의 상태를 추적하고, 변경 내용을 데이터베이스에 반영한다.

JPA의 EntityManager가 영속성 컨텍스를 통해 엔티티를 관리한다.

### 2-2. 엔티티의 4가지 상태

<table>
    <thead>
        <th>상태</th>
        <th>설명</th>
    </thead>
    <tbody>
        <tr>
            <td>비영속 (Transient)</td>
            <td>생성되었지만 영속성 컨텍스트에서 관리되지 않는 상태</td>
        </tr>
        <tr>
            <td>영속 (Managed)</td>
            <td>영속성 컨텍스트에서 관리되는 상태</td>
        </tr>
        <tr>
            <td>준영속 (Detached)</td>
            <td>관리되던 엔티티가 영속성 컨텍스트에서 분리된 상태</td>
        </tr>
        <tr>
            <td>삭제 (Removed)</td>
            <td>삭제 대상으로 등록된 상태</td>
        </tr>
    </tbody>
</table>

```java
Product product = new Product(); // 비영속

entityManager.persist(product); // 영속

entityManager.detach(product); // 준영속

entityManager.merge(product); // 준영속 → 영속 (반환된 엔티티가 영속 상태)

entityManager.remove(product); // 삭제
```

merge()는 전달한 객체 자체를 다시 영속 상태로 만드는 것이 아니라, 관리되는 엔티티를 반환한다.

### 2-3. 영속성 컨텍스트의 주요 이점

#### ① 1차 캐시 (1st-Level Cache)

영속성 컨텍스트는 엔티티를 식별자(ID)와 함께 관리한다.

```java
Product a = entityManager.find(Product.class, 1L);
Product b = entityManager.find(Product.class, 1L);

System.out.println(a == b); // true
```

같은 영속성 컨텍스트에서 동일한 ID의 엔티티를 조회하면 기존에 관리하던 객체를 반환한다.

따라서 두 변수는 동일한 객체를 참조한다.

1차 캐시는 일반적인 애플리케이션 전체 캐시가 아니라 영속성 컨텍스트 단위로 동작한다.

### ② 쓰기 지연 (Transactional Write-Behind)

INSERT 등 변경 SQL을 쓰기 지연 저장소에 모아두었다가 Flush 시점에 데이터베이스로 전달한다.

```aiignore
persist() → 영속성 컨텍스트 → 쓰기 지연 SQL 저장소 → Flush → Database
```

단, ID 생성 전략 등에 따라 INSERT가 즉시 시행될 수도 있다.

### ③ 변경 감지 (Dirty Checking)

영속 상태의 엔티티를 수정하면 JPA가 변경 여부를 감지한다.

```java
Product product = entityManager.find(Product.class, 1L);

product.changePrice(new BigDecimal("15000"));

// 별도의 save() 없이 변경 감지
```

JPA는 엔티티를 처음 관리할 때의 스냅샷과 현재 상태를 비교하여 변경 사항이 있으면 UPDATE SQL을 실행한다.

<br>
<hr>

## 3. Dirty Checking & Flush

<br>

```aiignore
엔티티 조회 → 영속성 컨텍스트에 저장 → 최초 상태의 스냅샷 보관 → 엔티티 값 변경

→ Flush → 현재 상태와 스냅샷 비교 → 변경 사항이 있으면 UPDATE SQL 실행
```

Dirty Checking은 영속 상태의 엔티티에 적용된다.

준영속 상태의 엔티티를 수정해도 자동으로 UPDATE되지 않는다.

### 3-2. Flush란?

영속성 컨텍스트의 변경 내용을 데이터베이스에 동기화하는 작업이다.

<table>
    <thead>
        <th>구분</th>
        <th>Flush</th>
        <th>Commit</th>
    </thead>
    <tbody>
        <tr>
            <td>역할</td>
            <td>변경 SQL을 DB에 반영</td>
            <td>트랜잭션을 최종 확정</td>
        </tr>
        <tr>
            <td>트랜잭션 종료</td>
            <td>X</td>
            <td>O</td>
        </tr>
        <tr>
            <td>Rollback 가능</td>
            <td>O</td>
            <td>Commit 이후에는 일반적으로 불가능</td>
        </tr>
    </tbody>
</table>

**Flush = DB에 변경 내용을 전달 / Commit = 트랜잭션 최종 확정**

Flush가 실행되더라도 트랜잭션이 종료된 것은 아니므로 Rollback 할 수 있다.

### 3-3. Flush 발생 시점

기본 FlushMode.AUTO 기준으로 다음 상황에서 발생한다.

```aiignore
1. 트랜잭션 Commit 시점
2. 실행할 JPQL 쿼리와 미반영 변경 내용이 관련되어 동기화가 필요한 경우
3. entityManager.flush()를 명시적으로 호출한 경우
```

Flush는 영속성 컨텍스트를 비우는 작업이 아니다. 영속성 컨텍스트를 비우려면 clear()를 사용한다.

<br>
<hr>

## 4. QueryDSL

<br>

### 4-1. QueryDSL이란?

문자열 대신 JAVA 코드를 사용하여 타입 안전한 쿼리를 작성할 수 있도록 도와주는 라이브러리이다.

JPQL은 문자열 기반이므로 오타나 타입 오류를 실행 시점에 발견할 수 있다.

QueryDSL은 컴파일 시점에 상당수의 오류를 확인할 수 있으며, 동적 조건을 구성하기 편리하다.

### 4-2. Q클래스

엔티티를 기반으로 자동 생성되는 쿼리 전용 클래스이다.

```aiignore
Product.java → Annotation Processor → QProduct.java
```

Q클래스를 사용하면 엔티티의 필드를 Java 코드로 참조할 수 있다.

### 4-3. 동적 쿼리

검색 조건이 있을 때만 WHERE 절에 추가한다.

```java
.where(
        nameContains(name),
        priceGoe(minPrice),
        priceLoe(maxPrice)
)
```

where()에 전달된 null인 BooleanExpression을 무시한다.
``
따라서 조건별 메서드를 BooleanExpression으로 분리하면 가독성과 재사용이 향상된다.

- eq() : =
- goe() : 이상 (>=)
- loe() : 이하 (<=)
- contains() : 문자열 포함
- in() : 여러 값 중 하나

### 4-4. Join & Paging

**Join :** 연관된 엔티티의 데이터를 함께 조회한다.

```java
.join(product.category, category)
```

**Paging :** 조회 범위와 정렬을 지정한다.

```java
.offset(pageable.getOffset())
.limit(pageable.getPageSize())
.orderBy(product.id.desc())
```

전체 페이지 수가 필요한 경우 별도의 COUNT 쿼리를 수행한다.

### 4-5. DTO Projection

필요한 컬럼만 조회하여 DTO에 직접 담는 방식이다.

```java
.select(new QProductDto(
        product.name,
        product.price,
        product.stock
))
```

@QueryProjection을 사용하면 DTO의 생성자 타입을 컴파일 시점에 확인할 수 있다.

단, DTO가 QueryDSL 라이브러리에 의존하게 된다.

<br>
<hr>

## 5. 핵심 정리

<table>
    <thead>
        <th>개념</th>
        <th>핵심</th>
    </thead>
    <tbody>
        <tr>
            <td>Transaction</td>
            <td>여러 DB 작업을 하나의 작업 단위로 관리</td>
        </tr>
        <tr>
            <td>@Transactional</td>
            <td>Spring의 선언적 트랜잭션 관리</td>
        </tr>
        <tr>
            <td>Persistence Context</td>
            <td>JPA가 엔티티의 상태를 관리하는 공간</td>
        </tr>
        <tr>
            <td>1차 캐시</td>
            <td>동일 영속성 컨텍스트 내 엔티티 재사용</td>
        </tr>
        <tr>
            <td>Dirty Checking</td>
            <td>영속 엔티티의 변경 사항 자동 감지</td>
        </tr>
        <tr>
            <td>Flush</td>
            <td>영속성 컨텍스트와 DB 동기화</td>
        </tr>
        <tr>
            <td>Commit</td>
            <td>트랜잭션 최종 확정</td>
        </tr>
        <tr>
            <td>QueryDSL</td>
            <td>Java 코드 기반 타입 안전 쿼리</td>
        </tr>
        <tr>
            <td>BooleanExpression</td>
            <td>동적 검색 조건의 재사용</td>
        </tr>
        <tr>
            <td>Projection</td>
            <td>필요한 컬럼만 DTO로 직접 조회</td>
        </tr>
    </tbody>
</table>

<br>

**이번 학습의 핵심은 트랜잭션이 데이터의 정합성을 보장하고, 영속성 컨텍스트가 엔티티의 상태를 관리하며, QueryDSL이 복잡한 조회 쿼리를 안전하게 작성하도록 돕는다는 점이다.**