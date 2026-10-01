<br>

# Day 2. Spring AI와 LLM 구현

<br>

## 1. LLM의 Message 구조

<br>

LLM과의 대화는 단순한 문자열이 아니라 역할이 구분된 Message로 구성된다.

<br>

<table>
    <thead>
        <th>Message</th>
        <th>역할</th>
    </thead>    
    <tbody>
        <tr>
            <td>System Message</td>
            <td>AI의 역할, 행동 규칙, 응답 방향 설정</td>
        </tr>
        <tr>
            <td>User Message</td>
            <td>사용자의 질문이나 명령</td>
        </tr>
        <tr>
            <td>Assistant Message</td>
            <td>AI가 생성한 응답</td>
        </tr>
    </tbody>
</table>

<br>

### · System Message

AI에게 역할과 답변 기준을 부여하는 지시문이다.

예를 들어 AI에게 Java 전문 강사의 역할을 부여하거나, 답변을 초보자 수준에 맞추도록 설정할 수 있다.

```java
Message systemMessage = new SystemMessage(
        "당신은 Java 전문 강사입니다."
);
```

### · User Message

사용자가 실제로 전달하는 질문이다.

```java
Message userMessage = new UserMessage(
        "Spring Boot와 Spring MVC의 차이는 무엇인가요?"
);
```

### · Assistant Message

LLM이 생성한 응답을 나타낸다.

```java
Message assistantMessage = new AssistantMessage(
        "Spring Boot는 Spring 애플리케이션의 설정을 간소화합니다."
);
```


**중요:** Assistant Message 자체가 AI의 영구적인 기억을 의미하지 않는다. 이전 응답을 다음 요청에 다시 전달해야 대화의 연속성을 유지할 수 있다. 

<br>
<hr>

## 2. 대화 히스토리 관리

<br>

LLM API는 기본적으로 요청에 포함된 정보를 바탕으로 답변한다.

따라서 여러 번의 질문을 하나의 대화로 연결하려면 애플리케이션에서 이전 대화를 저장하고 다시 전달해야 한다.

<br>

### 대화 흐름
```aiignore
[첫 번째 요청]

SystemMessage
User Message 1 → LLM → Assistant Message 1 → 애플리케이션에서 대화 저장

[두 번째 요청]

System Message
User Message 1, Assistant Message 1, User Message 2 
→ LLM → Assistant Message 2
```

이때 대화를 구분하는 식별자로 conversationId를 사용할 수 있다.

예를 들어 사용자 A와 사용자 B가 동시에 질문을 하더라도 서로 다른 conversationId를 사용하면 대화의 스토리를 구분할 수 있다.

<br>
<hr>

## 3. Context와 Context Window

<br>

### Context

LLM이 현재 응답을 생성할 때 참고하는 전체 문맥이다.

대표적으로 다음 정보가 포함된다.

- System Message
- 이전 대화 히스토리
- 현재 사용자 질문
- RAG를 통해 검색한 외부 문서
- Few-Shot Learning을 위한 예시

### Context Window

모델이 한 번의 처리에서 다룰 수 있는 토큰의 범위이다.

Context Window가 클수록 많은 정보를 전달할 수 있지만, 사용량과 처리 비용을 함께 고려해야 한다.

대화가 길어진다고 모든 히스토리를 무조건 전달하는 것은 비효율적이다.

<br>
<hr>

## 4. Token Usage

<br>

토큰은 LLM이 텍스트를 처리하는 기본 단위이며 API 이용 비용을 계산하는 기준으로 사용된다.

<br>

<table>
    <thead>
        <th>구분</th>
        <th>설명</th>
    </thead>    
    <tbody>
        <tr>
            <td>Prompt Tokens</td>
            <td>입력 프롬프트에 사용된 토큰</td>
        </tr>
        <tr>
            <td>Completion Tokens</td>
            <td>AI가 생성한 응답의 토큰</td>
        </tr>
        <tr>
            <td>Total Tokens</td>
            <td>입력 및 출력 토큰의 합계</td>
        </tr>
    </tbody>
</table>

```aiignore
Prompt Tokens = System Message + 이전 대화 + 현재 질문

Completion Tokens = AI가 생성한 답변

Total Tokens = Prompt Tokens + Completion Tokens
```

<br>

### 토큰 비용 최적화

1. System Message를 명확하고 간결하게 작성한다.
2. 불필요한 이전 대화를 제거한다.
3. 오래된 대화는 요약하여 정리한다.
4. 서비스에 필요한 중요 정보만 선택적으로 유지한다.

### 대화 히스토리 최적화 전략

<table>
    <thead>
        <th>전략</th>
        <th>설명</th>
    </thead>    
    <tbody>
        <tr>
            <td>Sliding Window</td>
            <td>최근 N개의 메시지만 유지</td>
        </tr>
        <tr>
            <td>Summarization</td>
            <td>오래된 대화를 요약하여 유지</td>
        </tr>
        <tr>
            <td>Smart Filtering</td>
            <td>중요한 정보가 포함된 메시지를 선택적으로 유지</td>
        </tr>
    </tbody>
</table>

**핵심:** 대화의 연속성을 유지하면서 불필요한 토큰 사용을 줄이는 것이 중요하다.

<br>
<hr>

## 5. ChatClient

<br>

Spring AI에서 LLM과 통신할 수 있도록 제공하는 API이다.

```java
String answer = chatClient.prompt()
        .user("JPA란 무엇인가요?")
        .call()
        .content();
```

<table>
    <thead>
        <th>메서드</th>
        <th>역할</th>
    </thead>    
    <tbody>
        <tr>
            <td>prompt()</td>
            <td>새로운 요청 구성 시작</td>
        </tr>
        <tr>
            <td>system()</td>
            <td>AI의 역할 및 지시문 설정</td>
        </tr>
        <tr>
            <td>user()</td>
            <td>사용자 질문 설정</td>
        </tr>
        <tr>
            <td>messages()</td>
            <td>여러 Message를 한 번에 전달</td>
        </tr>
        <tr>
            <td>call()</td>
            <td>동기식 응답 방식 지정</td>
        </tr>
        <tr>
            <td>content()</td>
            <td>생성된 응답 텍스트 추출</td>
        </tr>
        <tr>
            <td>chatResponse()</td>
            <td>응답 내용과 메타데이터 조회</td>
        </tr>
        <tr>
            <td>stream()</td>
            <td>스트리밍 응답 방식 지정</td>
        </tr>
    </tbody>
</table>

참고로 동기 호출의 call() 뒤에서는 content()나 chatResponse()와 같은 최종 응답 메서드를 통해 실제 결과를 가져온다.

<br>
<hr>

## 6. ConcurrentHashMap과 대화 저장

<br>

웹 서버에서는 여러 사용자의 요청이 동시에 처리될 수 있다.

예를 들어 다음과 같은 상황이다.

```aiignore
사용자 A ── Thread A ──┐
                       ├── ChatService
사용자 B ── Thread B ──┘          │
                                 ▼
                           conversations
```

이때 여러 스레드가 공유하는 Map에 동시에 접근할 수 있다.

일반적인 HashMap은 동시 접근에 대한 안정성을 보장하지 않으므로, 여러 스레드가 사용하는 공유 저장소에는 ConcurrentHashMap을 활용할 수 있다.

```java
private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
```

저장 구조는 다음과 같다.

```aiignore
conversationId
       │
       ▼
  List<Message>
       │
       ├── UserMessage 1
       ├── AssistantMessage 1
       ├── UserMessage 2
       └── AssistantMessage 2
```

주의할 점은 ConcurrentHashMap이 Map 자체의 동시 접근은 관리하지만, Map 내부에 저장한 ArrayList까지 자동으로 스레드 안전하게 만들어 주는 것은 아니다.

<br>
<hr>

## 7. Flux와 스트리밍 응답

<br>

일반적인 LLM 호출은 답변이 완성된 후 결과를 반환한다.

반면 스트리밍 방식은 응답이 생성되는 과정에서 데이터를 여러 조각으로 전달한다.

<br>

### 일반 호출

```aiignore
사용자 질문 → LLM 응답 생성 → 최종 답변 반환 
```

### 스트리밍 호출

```aiignore
사용자 질문 
    ↓
LLM 
    ├── "Spring"
    ├── "AI는"
    ├── " LLM과"
    └── " 통신합니다."
            ↓
        실시간 전달
```

Flux<T>는 Reactor에서 제공하는 타입으로, 0개 이상의 데이터를 비동기적으로 전달하는 스트림을 표현한다.

```java
Flux<String> result = chatClient.prompt()
        .user("Spring AI를 설명해주세요.")
        .stream()
        .content();
```

<br>

<table>
    <thead>
        <th>구분</th>
        <th>일반 호출</th>
        <th>스트리밍 호출</th>
    </thead>    
    <tbody>
        <tr>
            <td>반환 타입</td>
            <td>String</td>
            <td>Flux<String></td>
        </tr>
        <tr>
            <td>호출방식</td>
            <td>call()</td>
            <td>stream()</td>
        </tr>
        <tr>
            <td>응답 전달</td>
            <td>완성된 응답 반환</td>
            <td>데이터 조각을 순차적으로 전달</td>
        </tr>
        <tr>
            <td>활용</td>
            <td>일반 API</td>
            <td>실시간 AI 채팅</td>
        </tr>
    </tbody>
</table>

<br>
<hr>

## 8. 핵심 요약

- LLM의 이전 대화 기억은 애플리케이션에서 관리해야 한다.
- conversationId를 이용하면 여러 대화 세션을 구분할 수 있다.
- Context Window와 Token Usage를 고려하여 히스토리를 최적화해야 한다.
- ChatClient는 Spring AI를 이용한 LLM 호출을 단순화한다.
- ConcurrentHashMap은 여러 스레드가 공유하는 Map을 안전하게 관리하는 데 사용한다.
- Flux는 AI 응답을 실시간으로 전달하는 스트리밍 구현에 활용할 수 있다.

<br>

