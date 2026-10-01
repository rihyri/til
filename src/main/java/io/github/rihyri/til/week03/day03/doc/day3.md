<br>

# Day 3. LLM Context 관리

<br>

## 1. LLM의 Context란?

<br>

LLM은 기본적으로 이전 대화를 영구적으로 기억하지 않는다.

따라서 이전 대화 내용을 다음 요청에도 전달하고 싶다면 애플리케이션에 직접 대화 내역을 젖아하고 필요한 내용을 다시 LLM에게 전달해야 한다.

즉,

```aiignore
사용자 질문 → 기존 대화 내역 조회 → 기존 대화 + 새로운 질문을 LLM에 전달

→ AI 응답 → 질문과 응답을 DB에 저장
```

와 같은 구조가 필요하다.

<br>
<hr>

## 2. LLM Context 저장 방법

<br>

### · Layer 1. Volatile Memory

메모리에 데이터를 저장하는 방식이다.

```aiignore
Application Memory / Redis 등
```

**장점**

- 접근 속도가 빠르다.
- 최근 대화를 관리하기 쉽다.

**단점**

- 데이터가 사라질 수 있다.
- 장기간 대화 이력을 관리하기 어렵다.

### · TTL

TTL(Time To Live)은 데이터가 유지되는 시간을 의미한다.

예를 들어 Redis에서 TTL을 30분으로 설정하면 해당 데이터는 30분이 지나면 자동으로 삭제된다.

```aiignore
conversation:123
TTL = 30분
```

따라서 최근 대화처럼 일정 시간 동안만 필요한 데이터를 관리할 때 사용할 수 있다.

<br>

### · Layer 2. Non-volatile Memory

RDBMS에 대화 내용을 저장하는 방식이다.

DB에 저장되기 때문에 애플리케이션이 종료되더라도 대화 기록이 유지된다.

또한 conversation ID를 이용하면 여러 채팅창의 대화를 각각 관리할 수 있다.

<br>

### · Layer 3. Semantic Memory

Vector Database를 이용해 의미적으로 비슷한 데이터를 검색하는 방식이다.

단순히 모든 과거 대화를 LLM에게 전달하는 것이 아니라 현재 질문과 관련 있는 과거 정보를 찾아서 전달할 수 있다.

```aiignore
사용자 질문 → Embedding → Vector 검색 → 관련된 과거 내용 조회 → LLM Context에 추가
```

RAG에서도 사용하는 방식이다.

<br>
<hr>

## 3. DB를 이용한 대화 관리

<br>

대화 데이터는 크게 두 종류로 나눌 수 있다.

```aiignore
ChatConversation 
    └── ChatMessage
```

<br>

### · ChatConversation

하나의 채팅방을 의미한다.

예를 들어 ChatGPT에서 새로운 대화창을 만드는 것과 비슷하다.

```aiignore
id
title
createdAt
updatedAt
```

### · ChatMessage

채팅창 안에서 발생한 하나의 메시지를 의미한다.

```aiignore
id
conversationId
role
status
message
token
createdAt
```

하나의 Conversation에는 여러 개의 Message가 존재하기 때문에 관계는 다음과 같다.

```aiignore
ChatConversation 1 : N ChatMessage
```

<br>
<hr>

## 4. Message Role

<br>

메시지가 누구의 메시지인지 구분한다.

```java
public enum ChatMessageType {
    USER,
    ASSISTANT,
    SYSTEM,
    SUMMARY
}
```

- USER : 사용자의 질문
- ASSISTANT : AI의 답변
- SYSTEM : AI에게 전달하는 시스템 지시사항
- SUMMARY : 과거 대화를 요약하는 메시지

<br>
<hr>

## 5. Message Status

<br>

메시지가 현재 Context에서 사용되는지를 상태로 관리한다.

```java
public enum StatusType {
    ACTIVE,
    INACTIVE,
    DELETED
}
```

예를 들어 오래된 대화를 요약했다면 원본 메시지는 INACTIVE로 변경하고 새롭게 만들어진 요약 메시지를 ACTIVE로 저장할 수 있다.

```aiignore
기존 메시지

USER ACTIVE
ASSISTANT ACTIVE
USER ACTIVE
ASSISTANT ACTIVE

      ↓ 요약
      
 기존 메시지 → INACTIVE
 
SUMMARY ACTIVE
최근 USER ACTIVE
최근 AI ACTIVE
```
<br>
<hr>

## 6. 왜 대화 요약이 필요한가?

<br>

대화를 무한정 LLM에게 전달할 수는 없다.

대화가 길어질수록 다음과 같은 문제가 발생한다.

- Prompt Token 증가
- API 비용 증가
- 응답 속도 저하
- 모델의 Context Window 제한

따라서 오래된 대화는 요약하고 최근 대화는 원본 그대로 유지하는 방식을 사용할 수 있다.

<br>
<hr>

## 7. Sliding Window

<br>

최근 N개의 메시지만 유지하는 전략이다.

예를 들어 최대 메시지가 20개라면

```java
private static final int MAX_HISTORY_MESSAGES = 20;
```

전체 메시지가 35개일 때

```aiignore
0 ---------------- 14 | 15 ---------------- 34

    오래된 메시지 15개      최근 메시지 20개
            ↓
           요약               그대로 사용
```

최종적으로 LLM에게는 다음과 같이 전달할 수 있다.

```aiignore
[과거 대화 요약]
[최근 메시지 20개]
[현재 사용자 질문]
```

<br>
<hr>

## 8. Stream의 map 이해하기

<br>

다음 코드는 DB의 ChatMessage를 Spring AI의 Message로 변환한다.

```java
messages.stream()
    .map(this::mapToStrpingAiMessage)
    .toList();
```

처음 보면 복잡하지만 일반적인 반복문으로 보면 이해하기 쉽다.

```java
List<Message> result = new ArrayList<>();

for (ChatMessage message : messages) {
    Message converted = mapToSpringAiMessage(message);
    result.add(converted);
}
```

람다식으로 표현하면

```java
.map(message -> mapToSpringAiMessage(message))
```

메서드 참조를 사용하면

```java
.map(this::mapToSpringAiMessage)
```

가 된다. 

즉 세 코드는 같은 의미다.

```aiignore
ChatMessage → mapToSpringAiMessage() → Spring AI Message
```

<br>
<hr>

## 9. 전체 Service 흐름

<br>

이번 수업에서 가장 중요한 부분이다.

```aiignore
POST /api/chat
      ↓
Controller
      ↓
PersistenceChatService.chat()
      ↓
대화방 존재 여부 확인
      ↓
없음 → Conversation 생성
있음 → 기존 Conversation 조회
      ↓
USER 메시지 DB 저장
      ↓
ACTIVE 메시지 조회
      ↓
메시지가 너무 많으면 오래된 메시지 요약
      ↓
ChatMessage → Spring AI Message 변환
      ↓
ChatClient 호출
      ↓
AI 응답
      ↓
ASSISTANT 메시지 DB 저장
      ↓
Response 반환
```

이번 코드에서는 AI에게 질문하는 것뿐만 아니라 **대화의 Context를 애플리케이션이 직접 관리한다는 것**이 핵심이다.

<br>
