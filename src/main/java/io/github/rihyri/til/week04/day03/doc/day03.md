<br>

# Week 04 - Day 03. Spring AI Advisor & Function Calling

<br>

##  1. Spring AI Advisor

<br>

`Advisor`는 `ChatClient`의 요청과 응답을 처리하는 과정에 공통 기능을 추가하는 Spring AI의 확장 기능이다.

Service에 AI 관련 부가 로직을 직접 작성하지 않고 별도의 Advisor로 분리할 수 있다.

대표적인 활용 사례는 다음과 같다.

- 이전 대화 기록 관리
- 요청 및 응답 로깅
- RAG 검색 결과 추가
- 사용자 입력 검증 및 응답 후처리

Advisor를 활용하면 Service는 LLM 호출에 집중하고, 공통 기능을 여러 곳에서 재사용할 수 있다.

<br>
<hr>

## 2. Advisor Chain

<br>

여러 Advisor를 등록하면 체인 형태로 실행된다.

```aiignore
사용자 요청 → Advisor A (Before) → Advisor B (Before) → LLM 호출

→ Advisor B (After) → Advisor A (After) → 최종 응답
```

요청은 순서대로 처리하고, 응답은 반대 순서로 처리한다.


### · getOrder()
Advisor의 실행 순위를 결정한다.

- 숫자가 작을수록 요청을 먼저 처리한다
- 같은 우선순위를 가진 Advisor끼리는 실행 순서를 보장하지 않는다.

### · Advisor Context

하나의 AI 요청을 처리하는 과정에서 Advisor들이 데이터를 공유하는 공간이다.

예를 들어 요청 단계에서 추출한 사용자 정보를 Context에 저장하고, 응답 단계에서 다시 사용할 수 있다. 

**Context는 이전 대화 기록을 장기간 저장하는 Chat Memory와는 다른 개념이다.**

<br>
<hr>

## 3. Chat Memory

<br>

LLM은 기본적으로 이전 API 요청을 자동으로 기억하지 않는다.

연속적인 대화를 구현하려면 이전 메시지를 저장하고, 새로운 질문과 함께 LLM에 전달해야 한다.

Spring AI에서는 이를 위해 ChatMemory 추상화를 제공한다.

<table>
    <thead>
        <th>구분</th>
        <th>Chat Memory</th>
        <th>Chat History</th>
    </thead>
    <tbody>
        <tr>
            <td>목적</td>
            <td>LLM이 대화 문맥을 이해하도록 지원</td>
            <td>전체 대화 내역 보관</td>
        </tr>
        <tr>
            <td>저장 범위</td>
            <td>필요한 대화 일부 또는 전체</td>
            <td>전체 대화</td>
        </tr>
        <tr>
            <td>관리 방식</td>
            <td>메시지 수, 시간, 토큰 기준 등</td>
            <td>기록 보존 중심</td>
        </tr>
        <tr>
            <td>활용</td>
            <td>연속적인 AI 대화</td>
            <td>대화 조회, 감사 및 분석</td>
        </tr>
    </tbody>
</table>

예를 들어 전체 채팅 기록이 100개여도 LLM에는 최근 10개만 전달할 수 있다.

즉, **사용자에게 보여줄 대화 기록과 LLM에게 전달할 대화 문맥은 반드시 같을 필요가 없다.**

<br>

### Conversation ID

서로 다른 대화를 구분하기 위한 식별자이다.

```aiignore
transaction A → A의 대화 기록
transaction B → B의 대화 기록
```

모든 사용자가 동일한 `Conversation ID`를 사용하면 서로의 대화가 섞일 수 있다.

따라서 사용자 또는 대화방별로 ID를 분리하는 것이 중요하다.

<br>
<hr>

## 4. Chat Memory 저장 및 관리

<br>

Spring AI는 대화 기록의 저장과 관리를 분리한다.

<table>
    <thead>
        <th>구성요소</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>ChatMemory</td>
            <td>저장할 메시지와 유지 범위 관리</td>
        </tr>
        <tr>
            <td>ChatMemoryRepository</td>
            <td>메시지를 실제 저장하고 조회</td>
        </tr>
        <tr>
            <td>MessageChatMemoryAdvisor</td>
            <td>ChatClient에 대화 기록을 연결</td>
        </tr>
    </tbody>
</table>

<br>

### MessageWindowChatMemory

최근 메시지만 유지하는 메모리 구현 방식이다.

`maxMessages`로 보관할 메시지의 최대 개수를 설정할 수 있으며, 오래된 메시지를 정리할 때 대화 턴의 경계도 고려한다.

메시지 개수는 사용자 질문 횟수와 다르다. 사용자 메시지와 AI 메시지는 각각 메시지로 계산된다.

<br>

### In-Memory와 영구 저장소

#### In-Memory

- 애플리케이션 메모리에 기록을 저장한다.
- 별도 DB 없이 간단하게 사용할 수 있다.
- 서버가 종료되면 기록이 사라진다.

#### 영구 저장소

- JDBC, Redis 등 외부 저장소를 활용할 수 있다.
- 서버 재시작 이후에도 대화 기록을 유지하도록 구성할 수 있다.
- 여러 서버가 동일한 데이터를 사용해야 하는 환경에 적합하다.

단, 저장소에 따라 Tool Calling 중간 메시지 지원 범위가 다를 수 있다.

<br>
<hr>

## 5. Function Calling

<br>

`Function Calling`은 LLM이 답변을 생성하는 과정에서 애플리케이션이 제공하는 외부 기능을 사용할 수 있도록 하는 기술이다.

예를 들어 LLM은 데이터베이스 조회, 계산, 외부 API 호출 등의 기능을 활용할 수 있다.

<br>

### 핵심 개념

**LLM은 함수를 직접 실행하지 않는다.**

LLM은 어떤 도구가 필요한지 판단하고 호출할 도구의 이름과 인자를 생성한다.

실제 실행은 Spring AI 애플리케이션에서 담당한다.

```aiignore
사용자 질문 → LLM이 Tool 필요 여부 판단 → Tool 이름과 인자 생성 

→ Spring AI가 Java 메서드 실행 → 실행 결과를 LLM에 전달 → 최종 답변 생성
```

Tool 실행 후 LLM이 추가 Tool 호출을 요청하는 경우 이 과정이 반복될 수 있다.

<br>
<hr>

## 6. Tool 등록 방식

<br>

Spring AI에서는 Java 메서드에 `@Tool`을 사용하여 LLM이 호출할 수 있는 기능으로 등록할 수 있다.

```java
@Tool(description = "상품 번호로 상품 정보를 조회합니다.")
public Product findProduct (String productId) {
    return productService.findById(productId);
}
```

<br>

### @Tool의 description

LLM이 도구를 선택할 때 참고하는 설명이다.

단순히 메서드 이름을 작성하기보다 해당 도구의 목적과 사용 상황을 명확하게 설명하는 것이 좋다.

### defaultTools()와 tools

<table>
    <thead>
        <th>구분</th>
        <th>defaultTools()</th>
        <th>tools()</th>
    </thead>    
    <tbody>
        <tr>
            <td>설정 위치</td>
            <td>ChatClient 생성 시</td>
            <td>개별 요청 시</td>
        </tr>
        <tr>
            <td>사용 목적</td>
            <td>공통으로 사용할 Tool</td>
            <td>특정 요청에 필요한 Tool</td>
        </tr>
        <tr>
            <td>특징</td>
            <td>기본 도구 등록</td>
            <td>요청에 따라 도구 지정</td>
        </tr>
    </tbody>
</table>

Tool을 등록했다고 반드시 실행되는 것은 아니다.

LLM이 도구를 사용하지 않고 답변할 수도 있다.

<br>
<hr>

## 7. Tool Calling에서 중요한 설계 원칙

<br>

### 7-1. Tool 실행 권한

LLM이 Tool을 선택할 수 있다는 것은 애플리케이션 기능에 접근할 수 있다는 의미이기도 하다.

따라서 모든 Tool을 무조건 제공하는 것은 위험할 수 있다.

특히 다음과 같은 기능은 별도의 검증이 필요하다.

- 개인정보 조회
- 데이터베이스 수정 및 삭제
- 결제 또는 주문 처리
- 외부 서비스에 데이터를 전송하는 기능

**LLM의 Tool 선택 결과를 사용자 권한 검증의 대체 수단으로 사용해서는 안 된다.**

### 7-2. Tool의 입력값 검증

LLM이 생성한 Tool 인자가 항상 올바른 것은 아니다.

예를 들어 존재하지 않는 상품 ID나 잘못된 날짜가 전달될 수 있다.

따라서 일반적인 API와 마찬가지로 입력값 검증, 예외 처리, 접근 권한 확인이 필요하다.

### 7-3. Tool 결과와 최종 답변

Tool이 올바른 값을 반환하더라도 LLM이 최종 답변을 만드는 과정에서 결과를 잘못 해석할 수 있다.

따라서 정확성이 중요한 서비스에서는 Tool 실행 결과와 최종 답변을 구분하여 확인해야 한다.

<br>
<hr>

## 8. Advisor와 Function Calling의 관계

<br>

두 기능은 서로 다른 목적을 가지지만 함께 사용할 수 있다.

<table>
    <thead>
        <th>구분</th>
        <th>Advisor</th>
        <th>Function Calling</th>
    </thead>    
    <tbody>
        <tr>
            <td>목적</td>
            <td>AI 호출 과정 확장</td>
            <td>외부 기능 활용</td>
        </tr>
        <tr>
            <td>실행 주체</td>
            <td>Spring AI Advisor Chain</td>
            <td>LLM의 요청에 따라 Spring AI가 도구 실행</td>
        </tr>
        <tr>
            <td>사용 예시</td>
            <td>대화 메모리, 로깅, RAG</td>
            <td>계산, 검색, API 호출</td>
        </tr>
        <tr>
            <td>핵심 관점</td>
            <td>요청을 어떻게 처리할 것인가?</td>
            <td>어떤 기능을 사용할 것인가?</td>
        </tr>
    </tbody>
</table>

최신 Spring AI에서는 `ToolCallingAdvisor`가 Tool Calling의 실행 과정을 Advisor Chain 내부에서 관리할 수 있다.

또한 메모리 Advisor의 실행 위치에 따라 Tool 호출 과정의 중간 메시지가 메모리에 포함되는 범위가 달라질 수 있다.

따라서 Advisor의 실행 순서는 단순한 우선순위 설정을 넘어 실제 처리 결과에도 영향을 줄 수 있다.

<br>
<hr>

## 정리

<br>

이번 학습의 핵심은 Spring AI가 LLM과 애플리케이션 사이의 기능을 어떻게 분리하고 확장하는지 이해하는 것이다.

- **Advisor** : LLM 요청과 응답을 처리하는 과정에 공통 로직 추가
- **Chat Memory** : 이전 대화를 관리하여 연속적인 대화 지원
- **Function Calling** : LLM이 애플리케이션의 외부 기능을 활용하도록 연결

특히 LLM의 판단과 애플리케이션의 실행 책임은 구분해야 한다.

Tool의 선택은 LLM이 수행할 수 있지만, 실제 실행과 권한 검증, 오류 처리는 개발자가 설계한 애플리케이션에서 담당한다.

<br>

