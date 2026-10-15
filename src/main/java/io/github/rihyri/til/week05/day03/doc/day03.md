<br>

# Week05 - Day 03. Redis 캐싱 전략과 성능 관리

<br>

## 1. 캐싱 (Cache) 이란?

<br>

캐싱은 자주 사용하는 데이터를 빠르게 접근할 수 있는 저장소에 보관하여 반복적인 데이터 조회 비용을 줄이는 기술이다.

일반적인 애플리케이션이 동일한 데이터를 요청할 때마다 DB에 접근하지 않고, Redis 등에 저장된 데이터를 우선 활용한다.

<br>

### 캐싱의 장단점

#### 장점
- DB 조회 횟수를 줄여 응답 속도를 개선할 수 있다.
- 반복적인 연산과 조회를 최소화한다.
- DB 및 외부 API의 부하를 줄이는 데 도움이 된다.

#### 단점
- DB와 캐시 사이에 데이터 불일치가 발생할 수 있다.
- 캐시를 저장할 메모리 공간이 필요하다.
- 데이터 변경 시 캐시 갱신 또는 무효화 전략을 고려해야 한다.

따라서 모든 데이터를 캐싱하기보다는 조회 빈도와 변경 주기를 고려해 적용하는 것이 좋다. 

<br>
<hr>

## 2. 캐싱 전략의 종류

<br>

캐싱 전략은 데이터를 언제 저장하고, 언제 갱신하거나 삭제하는지에 따라 구분할 수 있다.

<br>

### 2-1. Cache-aside

애플리케이션에서 데이터를 조회할 때 캐시를 먼저 확인하는 방식이다.

#### 동작 과정

1. Redis에 데이터가 존재하는지 확인한다.
2. 데이터가 있으면 캐시 값을 반환한다. (Cache Hit)
3. 데이터가 없으면 DB에서 조회한다. (Cache Miss)
4. DB 조회 결과를 Redis에 저장하고 반환한다. 

#### 특징
- 읽기 요청이 많은 서비스에 적합하다.
- 필요한 데이터만 캐싱하므로 메모리를 효율적으로 활용할 수 있다.
- 최초 캐시 미스 시 DB 조회가 발생한다.

예를 들어 자주 조회하지만 변경은 적은 카테고리 목록, 상품 정보 등에 활용할 수 있다.

<br>

### 2-2. Write-through

데이터를 저장하거나 변경할 때 원본 저장소와 캐시를 함께 갱신하는 방식이다.

#### 동작 과정
1. 데이터 변경 요청을 받는다.
2. DB에 데이터를 저장한다.
3. DB 저장 성공을 확인한 뒤 캐시에도 최신 데이터를 반영한다.

#### 특징
- 캐시를 최신 상태로 관리하는 데 유리하다.
- DB와 Redis에 각각 쓰기 작업이 필요하다.
- 두 저장소의 작업을 하나의 트랜잭션으로 묶기 어렵다.

Spring에서는 DB 커밋 후 이벤트를 활용해 캐시 갱신을 수행할 수 있다. 단, 갱신 실패와 동시성 문제는 별도로 대응해야 한다.

<br>

## 2-3. Write-back

데이터를 우선 빠른 저장소에 기록한 다음, 일정 시간이 지난 후 DB에 반영하는 방식이다.

### 동작 과정
1. 데이터 변경 요청을 Redis 등에 기록한다.
2. 애플리케이션은 DB 반영을 기다리지 않고 응답할 수 있다.
3. 별도 작업자가 대기 중인 데이터를 DB에 반영한다.

### 특징
- DB 쓰기 부하를 분산할 수 있다.
- 쓰기 요청이 빈번한 환경에 활용할 수 있다.
- DB 반영 전에 장애가 발생하면 데이터 유실 위험이 있다. 
- 재시도, 멱등성, 데이터 순서 보장 등을 고려해야 한다.

단순히 `@Async`로 DB 저장을 호출하는 것만으로 안전한 Write-back이 완성되는 것은 아니다.

<br>

### 2-4. Write-around

데이터를 변경할 때 DB에 직접 저장하고, 해당 데이터의 캐시를 갱신하지 않는 방식이다. 

일반적으로 기존 캐시를 무효화하여 오래된 데이터가 사용되지 않도록 한다. 

#### 동작 과정
1. DB에서 데이터를 변경한다.
2. 변경된 데이터와 관련된 기존 캐시를 삭제한다.
3. 다음 조회 요청 시 DB 데이터를 다시 가져와 캐시를 생성한다.

#### 특징
- 쓰기 작업 시 캐시 갱신 비용을 줄일 수 있다.
- 캐시에 오래된 데이터가 남지 않도록 관리하기 쉽다.
- 변경 후 첫 조회에서는 Cache Miss가 발생할 수 있다.


실무에서는 Cache-aside와 함께 사용하기 좋다.

<br>

<table>
    <thead>
        <th>전략</th>
        <th>캐시 처리 시점</th>
        <th>주요 목적</th>
    </thead>
    <tbody>
        <tr>
            <td>Cache-aside</td>
            <td>조회 시</td>
            <td>읽기 성능 개선</td>
        </tr>
        <tr>
            <td>Write-through</td>
            <td>쓰기 시</td>
            <td>최신 캐시 유지</td>
        </tr>
        <tr>
            <td>Write-back</td>
            <td>DB 반영 전</td>
            <td>쓰기 성능 개선</td>
        </tr>
        <tr>
            <td>Write-around</td>
            <td>쓰기 시 캐시 우회/삭제</td>
            <td>불필요한 캐시 갱신 방지</td>
        </tr>
    </tbody>
</table>

<br>
<hr>

## 3. Redis TTL (Time To Live)

`TTL`은 `Redis Key`가 유지되는 시간을 의미한다.

TTL을 설정하면 지정된 시간이 지난 데이터는 만료되어 더 이상 조회되지 않는다.

<br>

#### TTL을 사용하는 이유

- 사용하지 않는 데이터를 자동으로 정리한다.
- 메모리를 효율적으로 관리할 수 있다.
- 오래된 캐시가 무기한 유지되는 것을 방지한다.

<br>

#### Redis 주요 명령어

```
SET category:1 "Backend" EX 300
TTL category:1
EXPIRE category:1 600
PERSIST category:1
```

- `SET ... EX` : 저장하면서 만료 시간 설정
- `TTL` : 남은 만료시간 확인
- `EXPIRE` : 만료 시간 설정 또는 변경
- `PERSIST` : 만료 시간 제거

TTL 명령어의 결과가 -1이면 만료 시간이 없고, -2이면 키가 존재하지 않는다는 뜻이다.

<br>

### Spring Redis에서 TTL 설정

```java
redisTemplate.opsForValue().set(
        "category:tree",
        jsonData,
        Duration.ofMinutes(30)
);
```

TTL은 데이터의 변경 빈도와 서비스 요구사항을 고려해 설정해야 한다.

TTL만으로 데이터의 최신 상태가 항상 보장되는 것은 아니므로, 수정과 삭제 시 캐시 무효화도 함께 고려한다.

<br>
<hr>

## 4. Redis 메모리 관리: LRU와 LFU

<br>

Redis는 메모리 기반 저장소이므로 사용할 수 있는 메모리에 한계가 있다.

`maxmemory`는 Redis가 사용할 메모리 제한을 설정하고, `maxmemory-policy`는 메모리 제한에 도달했을 때 어떤 데이터를 제거할지 결정한다.

<br>

### LRU (Least Recently Used)

가장 오랫동안 사용되지 않은 데이터를 우선 제거하는 정책이다.

최근 접근한 데이터가 다시 사용될 가능성이 높다는 특성을 활용한다.

```aiignore
CONFIG SET maxmemory-policy allkeys-lru
```

<br>

### LFU (Least Frequently Used)

접근 빈도가 낮은 데이터를 우선 제거하는 정책이다.

일정 기간 지속적으로 자주 사용되는 데이터를 유지하는 데 유리하다.

```aiignore
CONFIG SET maxmemory-policy allkeys-lfu
```

Redis의 LRU와 LFU는 정확한 사용 이력을 모두 기록하는 대신 근사 알고리즘을 사용한다.

<br>

### 기타 제거 정책

<table>
    <thead>
        <th>정책</th>
        <th>설명</th>
    </thead>
    <tbody>
        <tr>
            <td>noeviction</td>
            <td>메모리 제한 시 새 데이터 쓰기 거부</td>
        </tr>
        <tr>
            <td>allkeys-lru</td>
            <td>모든 키 중 최근 사용이 적은 키 제거</td>
        </tr>
        <tr>
            <td>allkeys-lfu</td>
            <td>모든 키 중 사용 빈도가 낮은 키 제거</td>
        </tr>
        <tr>
            <td>volatile-lru</td>
            <td>TTL이 설정된 키 중 LRU 적용</td>
        </tr>
        <tr>
            <td>volatile-ttl</td>
            <td>TTL이 설정된 키 중 만료가 가까운 키 제거</td>
        </tr>
    </tbody>
</table>

<br>

### TTL과 Eviction의 차이
- TTL : 정해진 만료 시간에 따라 데이터가 만료된다.
- Eviction : 메모리 제한에 도달했을 때 설정된 정책에 따라 데이터를 제거한다.

두 기능은 서로 다른 목적으로 동작한다.

<br>
<hr>

## 5. Redis 데이터 영속성

Redis는 메모리에 데이터를 저장하므로 서버 장애 시 데이터가 유실될 수 있다.

이를 보완하기 위해 디스크에 데이터를 기록하는 영속성 기능을 제공한다.

<br>

### 5-1. RDB (Redis Database)

특정 시점의 Redis 데이터를 스냅샷으로 저장하는 방식이다.

#### 장점
- 백업 파일을 관리하기 편리하다.
- 대규모 데이터 복구에 유리하다.

#### 단점
- 스냅샷 사이에 변경된 데이터가 유실될 수 있다.
- 스냅샷 생성 시 시스템 자원이 추가로 사용된다.

```aiignore
CONFIG SET save "60 1000"
```

60초 동안 1,000개 이상의 변경이 발생하면 스냅샷 저장 조건을 만족하도록 설정하는 예시이다.

<br>

### 5-2. AOF (Append Only File)

Redis에서 실행된 쓰기 명령을 로그로 기록하는 방식이다.

#### 장점
- RDB보다 최근 변경 사항을 복구하는 데 유리하다.
- 기록된 명령을 재실행하여 데이터를 복원할 수 있다.

### 단점
- 로그 기록으로 인한 I/O 비용이 발생한다.
- 설정에 따라 파일 용량과 성능 부담이 증가할 수 있다.

```aiignore
CONFIG SET appendonly yes
CONFIG SET appendfsync everysec
```

`everysec` 설정은 일반적으로 약 1초 간격으로 디스크 동기화를 시도한다. 다만 실제 유실 범위는 장애 상황에 따라 달라질 수 있다.

<br>

#### RDB와 AOF 비교

<table>
    <thead>
        <th>구분</th>
        <th>RDB</th>
        <th>AOF</th>
    </thead>
    <tbody>
        <tr>
            <td>저장 방식</td>
            <td>시점별 스냅샷</td>
            <td>쓰기 명령 기록</td>
        </tr>
        <tr>
            <td>복구 특성</td>
            <td>대규모 복구에 유리</td>
            <td>최근 변경 복구에 유리</td>
        </tr>
        <tr>
            <td>데이터 유실</td>
            <td>스냅샷 사이 유실 가능</td>
            <td>동기화 정책에 따라 발생</td>
        </tr>
        <tr>
            <td>주 용도</td>
            <td>백업, 복구</td>
            <td>높은 데이터 보존 요구</td>
        </tr>
    </tbody>
</table>


서비스 요구사항에 따라 두 방식을 병행하거나, 재생성 가능한 순수 캐시에서는 영속성을 비활성화할 수 있다. 

<br>
<hr>

## 6. Spring에서 Redis 캐싱 구현하기

<br>

Spring Data Redis의 `RedisTemplate`을 사용하면 Redis 명령을 Java 메서드로 실행할 수 있다.

```java
// 조회
redisTemplate.opsForValue().get(key);

// 저장
redisTemplate.opsForValue().set(key, value);

// TTL을 포함한 저장
redisTemplate.opsForValue().set(
        key, value, Duration.ofMinutes(30)
);

// 삭제
redisTemplate.delete(key);
```

<br>
<hr>

## 7. 핵심 정리

<br>

- **Cache-aside**는 조회 성능을 개선하기 위한 대표적인 캐싱 전략이다.
- **Write-through**는 쓰기 작업 후 캐시도 갱신하는 방식이다.
- **Write-back**은 쓰기 응답과 DB 반영을 분리하지만, 장애 복구가 중요하다.
- **Write-around**는 DB 변경 후 기존 캐시를 삭제하는 방식과 함께 사용할 수 있다.
- **TTL**은 데이터의 만료 시점을 관리한다.
- **LRU/LFU**는 메모리 부족 시 어떤 키를 제거할지 결정한다.
- **RDB/AOF**는 Redis 데이터의 복구 가능성을 높이는 영속성 기능이다.

**결론 :** Redis 캐싱은 단순히 데이터를 빠르게 조회하는 기술을 넘어, 데이터 일관성, 메모리 관리, 장애 복구 전략까지 함께 고려해야 하는 기술이다.

<br>

