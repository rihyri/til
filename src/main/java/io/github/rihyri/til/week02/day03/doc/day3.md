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