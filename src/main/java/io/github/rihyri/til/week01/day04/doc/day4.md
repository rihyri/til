
<br>

# Week 1 - Day 4. Self-Reference, DTO와 JPA 데이터 로딩 전략

<br>

## 1. 자기 참조 (Self-Referencing)

<br>

자기 참조는 **하나의 테이블이 자기 자신의 데이터를 참조하는 구조**이다.

대표적으로 카테고리, 메뉴, 댓글처럼 계층 구조를 표현할 때 사용한다.

```aiignore
id  | name      | parent_id
----------------------------
1   | 의류       | null
2   | 여성복     | 1
3   | 남성복     | 2
```
`의류`는 부모가 없는 최상위 데이터이고, `여성복`, `남성복`은 `의류`를 부모로 가진다.

```aiignore
의류
├─ 여성복
└─ 남성복
```

<br>

### Root와 Child

#### Root

- 최상위 데이터
- 부모가 존재하지 않기 때문에 `parent_id = null`


#### Child

- 특정 부모를 참조하는 데이터
- 예를 들어 `parent_id = 1`이면 1번 데이터를 부모로 가진다.

`depth1`, `depth2`, `depth3`처럼 컬럼을 직접 만드는 방법도 있지만 계층이 늘어날 때마다 컬럼을 추가해야 한다.

따라서 계층 구조가 유동적이라면 자기 참조 방식이 더 유연하다.

<br>
<hr>

## 2. DTO (Data Transfer Object)

<br>

DTO는 **계층 간 데이터를 전달하기 위한 객체**이다.

Entity를 API 요청과 응답에 그대로 사용하지 않고 DTO를 별도로 만들어 사용하면 API에서 필요한 데이터만 전달할 수 있다.

```aiignore
Client
  ↓ Request DTO
Controller
  ↓
Service
  ↓
Repository → Entity
  ↓
Service
  ↓ Entity → Response DTO 변환
Controller
  ↓
Client
```

보통 이름은 다음과 같이 사용할 수 있다.

- `CategoryDTO` : 일반적인 DTO
- `CategoryRequest` : API 요청 데이터
- `CategoryResponse` : API 응답 데이터

프로젝트에 따라 `Request`, `Response`, `Parent`, `Child` 등을 하나의 DTO 클래스 내부에 **static inner class**로 묶어서 관리하기도 한다.

```java
public class CategoryDto {
    
    public static class Request {
    }
    
    public static class Response {
    }
    
    public static class Parent {
    }
    
    public static class Child {
    }
}
```

관련 DTO를 하나의 클래스에 모을 수 있다는 것이 장점이다.

`@Schema(name = "categoryRequest")`는 Swagger/OpenAPI 문서에 표시되는 객체의 이름을 지정할 때 사용한다.

<br>
<hr>

## 3. Entity → DTO 변환

<br>

Repository가 반환하는 것은 보통 Entity이다.

하지만 Controller가 Entity를 그대로 반환하기보다는 Service에서 필요한 값만 골라 DTO로 변환해서 반환하는 것이 좋다.

예를 들어 다음 Entity 목록이 있다고 하자.

```aiignore
Menu Entity
Menu Entity
Menu Entity
```

이를 API 응답용 객체로 바꾸면 다음과 같은 과정이 된다.

```aiignore
Entity
  ↓
 DTO

Entity
  ↓
 DTO

Entity
  ↓
 DTO
 
  ↓  toList()
List<DTO>
```
Java Stream에서는 다음과 같이 표현할 수 있다.

```aiignore
entities.stream()
    .map(entity -> DTO로 변환)
    .toList();
```

즉,

```java
List<Entity>
```

를

```java
List<DTO>
```

로 바꾸는 과정이라고 이해하면 된다.

<br>

## 4. Foreign Key

<br>

Foreign Key는 테이블 사이의 관계를 DB가 직접 보장하도록 만든다.

<br>

### 장점

데이터 무결성을 보장할 수 있다.

존재하지 않는 부모 데이터를 참조하거나 잘못된 관계가 만들어지는 것을 DB 수준에서 막을 수 있다.


### 단점

관계 검증에 따른 비용이 발생할 수 있고, 대규모 시스템에서는 데이터 구조 변경이나 서비스 분리에 제약이 생길 수 있다.

따라서 무조건 사용하거나 사용하지 않는 것이 아니라 시스템의 데이터 무결성, 성능, 확장성을 고려해야 한다.


<br>
<hr>

## 5. 정규화와 반정규화

<br>

### 정규화

데이터 중복을 줄이고 데이터 구조를 올바르게 나누는 과정이다. 목적은 데이터 중복과 수정 이상 현상을 줄이는 것이다.

주로 제1정규화, 제2정규화, 제3정규화까지 많이 다룬다. 

### 반정규화

조회 성능 등을 위해 의도적으로 데이터를 합치거나 중복해서 저장하는 방식이다.

```aiignore
정규화 
→ 데이터 중복 감소
→ 데이터 무결성 관리에 유리

반정규화
→ 조회 구조 단순화
→ 조회 성능 개선 가능
```

실무에서는 둘 중 하나만 고집하기보다 상황에 따라 적절하게 사용한다.

<br>
<hr>

# JPA 데이터 로딩 전략

## 6. EAGER과 LAZY

<br>

ORM에서는 DB의 데이터를 Entity로 언제 가져올지 결정해야 한다.

이를 데이터 로딩 전략이라고 한다.

<br>

### EAGER

연관된 데이터를 **즉시 함께 조회**한다.

```java
@ManyToOne(fetch = FetchType.EAGER)
```

Entity를 조회할 때 당장 사용하지 않는 데이터까지 조회될 수 있다는 단점이 있다.

### LAZY

연관된 데이터가 **실제로 필요한 시점에 조회**된다.

```java
@ManyToOne(fetch = FetchType.LAZY)
```

실무에서는 연관관계의 조회 시점을 예측하기 쉽도록 `LAZY`를 기본으로 사용하는 경우가 많다.

단, **LAZY 자체가 N+1 문제를 해결해주는 것은 아니다.**

<br>
<hr>

<br>

## 7. N+1 문제

<br>

처음 데이터를 조회하는 쿼리 1번 이후, 연관 데이터를 조회하기 위해 추가 쿼리를 N번 실행되는 문제이다.

예를 들어 주문 3개를 조회했다고 가정하자.

```aiignore
주문 전체 조회 → 1번

주문1의 회원 조회 → 1번
주문2의 회원 조회 → 1번
주문3의 회원 조회 → 1번

∴ 총 4번
```

데이터가 100개라면 추가 쿼리도 최대 100번 발생할 수 있다.

특히 LAZY 연관관계를 반복문에서 하나씩 접근할 때 쉽게 발생할 수 있다.

<br>
<hr>

## 8. N+1 해결 방법

### Fetch Join

JPQL에서 연관된 Entity를 처음부터 함께 조회하는 방식이다.

```java
@Query("""
        SELECT o
        FROM Order o
        JOIN FETCH o.member
        """)
List<Order> findAllWithMember();
```

필요한 데이터를 한 번에 조회할 수 있다.

단, `OneToMany` 같은 **컬렉션 Fetch Join과 Pagination을 같이 사용할 때는 주의해야 한다.**

### @BatchSize

지연 로딩이 필요한 데이터를 하나씩 조회하지 않고 일정 개수씩 묶어서 조회하도록 한다.

```aiignore
@BatchSize(size = 100)

* 하나씩 조회
1 → SELECT
2 → SELECT
3 → SELECT

* Batch 조회
1, 2, 3 → SELECT ... WHERE id IN (...)
```

Fetch Join이 **처음부터 같이 가져오는 방식**이라면, BatchSize는 **필요해졌을 때 여러 개를 묶어서 가져오는 방식**이라고 이해하면 쉽다.

<br>
<hr>

## 마치며

<br>

<table>
    <thead>
        <th>개념</th>
        <th>핵심</th>
    </thead>
    <tr>
        <td>Self-Reference</td>
        <td>자신의 테이블을 다시 참조해 계층 구조 표현</td>
    </tr>
    <tr>
        <td>DTO</td>
        <td>API에서 필요한 데이터만 전달</td>
    </tr>
    <tr>
        <td>Inner Class DTO</td>
        <td>관련 DTO를 하나의 클래스에 그룹화</td>
    </tr>
    <tr>
        <td>Foreign Key</td>
        <td>DB 수준에서 관계와 무결성 보장</td>
    </tr>
    <tr>
        <td>정규화</td>
        <td>데이터 중복 감소</td>
    </tr>
    <tr>
        <td>반정규화</td>
        <td>조회 성능 등을 위해 데이터 구조를 일부 중복</td>
    </tr>
    <tr>
        <td>EAGER</td>
        <td>연관 데이터 즉시 조회</td>
    </tr>
    <tr>
        <td>LAZY</td>
        <td>필요한 시점에 연관 데이터 조회</td>
    </tr>
    <tr>
        <td>N + 1</td>
        <td>최초 조회 후 연관 데이터 조회 쿼리가 반복 발생</td>
    </tr>
    <tr>
        <td>Fetch Join</td>
        <td>필요한 연관 데이터를 처음부터 함께 조회</td>
    </tr>
    <tr>
        <td>BatchSize</td>
        <td>지연 조회 데이터를 일정 개수씩 묶어서 조회</td>
    </tr>
</table>

<br>