<br>

# Week 3. Spring AI & LLM

<br>

3주차에는 Spring Boot 애플리케이션에서 LLM을 사용하는 방법을 시작으로, 대화의 Context를 관리하고 이미지를 함께 처리하는 Multimodal까지 학습했다.

단순히 AI API에 질문을 보내는 것에 끝나는 것이 아니라, 실제 채팅 서비스를 만들기 위해서는 **이전 대화를 어떻게 저장하고, 필요한 Context만 LLM에게 전달할 것인지**를 애플리케이션에서 직접 관리해야 한다는 점이 이번 주의 핵심이었다.

전체적인 흐름은 다음과 같다.

```azure
사용자 → Controller → Chat Service → 이전 대화 조회 → Context 구성
→ ChatClient → LLM → AI 응답 → 대화 저장 → 사용자에게 응답
```

<br>
<hr>

## 1. Spring AI와 ChatClient

<br>

Spring AI는 Spring 애플리케이션에서 다양한 AI 모델을 보다 쉽게 사용할 수 있도록 추상화된 API를 제공한다.

그 중 `ChatClient`는 LLM에게 Prompt를 전달하고 응답을 가져오는 역할을 한다.

```java
String answer = chatClient.prompt()
        .user("JPA란 무엇인가요?")
        .call()
        .content();
```

각 메서드의 역할은 다음과 같다.

<table>
    <thead>
        <th>메서드</th>
        <th>역할</th>
    </thead>
    <tbody>
        <tr>
            <td>prompt()</td>
            <td>새로운 요청 구성</td>    
        </tr>
        <tr>
            <td>system()</td>
            <td>AI의 역할 및 지시사항 설정</td>    
        </tr>
        <tr>
            <td>user()</td>
            <td>사용자 질문 전달</td>    
        </tr>
        <tr>
            <td>messages()</td>
            <td>여러 Message 전달</td>    
        </tr>
        <tr>
            <td>content()</td>
            <td>AI의 응답 문자열 추출</td>    
        </tr>
        <tr>
            <td>chatResponse()</td>
            <td>응답과 메타데이터 조회</td>    
        </tr>
        <tr>
            <td>stream()</td>
            <td>스트리밍 방식으로 응답</td>    
        </tr>
    </tbody>
</table>

기존의 Spring 애플리케이션이 Repository 등을 이용해 외부 데이터에 접근했다면, AI 기능에서는 `ChatClient`가 애플리케이션과 LLM 사이의 통신을 담당한다고 이해할 수 있다.

<br>
<hr>

## 2. LLM의 Message와 Context

<br>

LLM과의 대화는 단순한 문자열이 아니라 역할을 가진 Message로 구성된다.

```azure
System Message → AI의 역할과 규칙

User Message → 사용자의 질문

Assistant Message → AI가 생성한 답변
```

여기서 가장 중요했던 점은 **LLM이 이전 대화를 자동으로 기억하는 것이 아니라는 것**이다.

예를 들어 첫 번째 요청에서

```azure
User : 내 이름은 홍길동이야.
Assistant : 반갑습니다.
```

라는 대화를 했더라도 다음 요청에서 이전 Message를 다시 전달하지 않는다면

```azure
User : 내 이름이 뭐였지?
```

라는 질문에 이전 내용을 기반으로 답하기 어렵다.

따라서 대화를 이어가기 위해서는 애플리케이션과 이전 대화를 저장하고 매 요청마다 필요한 대화를 다시 전달해야 한다.

```azure
System Message + 이전 대화 + 현재 질문 → LLM
```

이렇게 LLM이 현재 답변을 생성하기 위해 참고하는 전체 정보를 **Context**라고 한다.

<br>
<hr>

## 3. Context Window와 Token

LLM에게 모든 과거 대화를 계속 전달할 수 없다.

모델마다 한 번에 처리할 수 있는 토큰의 범위인 **Context Window**가 존재하고, 대화가 길어질수록 Prompt Token도 계속 증가하기 때문이다.

```azure
Prompt Token 
=
System Message + 이전 대화 + 현재 질문 + 외부 검색 정보 등
```

대화가 계쏙 쌓이면 다음과 같은 문제가 발생할 수 있다.

- API 비용 증가
- 응답 속도 저하
- Context Window 초과
- 불필요한 대화까지 모델에 전달

따라서 실제 서비스에서는 Context를 적절히 줄이는 전략이 필요하다.

대표적인 방법은 다음과 같다.

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
            <td>오래된 대화를 요약하며 유지</td>
        </tr>
        <tr>
            <td>Smart Filtering</td>
            <td>현재 질문과 중요한 정보반 선택</td>
        </tr>
    </tbody>
</table>

핵심은 단순히 Context를 많이 전달하는 것이 아니라 **대화의 의미는 유지하면서 불필요한 Token을 줄이는 것**이다.

<br>
<hr>

## 4. 대화 Context를 DB에서 관리하기

<br>

이번 주에는 대화를 단순히 메모리에 저장하는 것을 넘어 DB를 이용하여 관리하는 구조도 학습했다.

대화 데이터는 크게 두 가지로 구분할 수 있다.

```azure
ChatConversation - 1 : N → ChatMessage
```

`ChatConversation`은 하나의 채팅방을 의미하고, `ChatMessage`는 채팅방 안에서 발생한 각각의 메시지를 의미한다.

```azure
* ChatConversation
- id
- title
- createdAt
- updatedAt

* ChatMessage
- id 
- conversationId
- role
- status
- message
- token
- createdAt
```

`conversationId`를 기준으로 메시지를 조회하면 여러 사용자의 대화 또는 여러 채팅방을 각각 분리하여 관리할 수 있다.

<br>
<hr>

## 5. Sliding Window와 대화 요약

<br>

이번 주에서 가장 중요했던 부분 중 하나는 **대화가 많아졌을 때 오래된 메시지를 처리하는 방법**이었다.

예를 들어 최근 메시지를 최대 20개만 유지한다고 가정한다.

```azure
전체 메시지 35개

[ 오래된 메시지 15개 ] [ 최근 메시지 20개 ]
        ↓
       요약              그대로 유지
```

최종적으로 LLM에게 전달되는 Context는 다음과 같이 구성할 수 있다.

```azure
[과거 대화 SUMMARY]
[최근 메시지 20개]
[현재 사용자 질문]
```

이때 요약된 과거 메시지를 바로 삭제하는 대신 상태를 관리할 수 있다.

```azure
* 기존 메시지
USER    ACTIVE             기존 메시지 → INACTIVE
ASSISTANT ACTIVE    →      SUMMARY  ACTIVE
USER ACTIVE                최근 USER  ACTIVE
ASSISTANT ACTIVE           최근 AI    ACTIVE
```

`ACTIVE` 메시지만 조회하면 다음 요청에서는 오래된 원본 대화 대신 SUMMARY와 최근 대화만 LLM에게 전달할 수 있다.

이 구조를 통해 원본 대화 기록은 DB에 남겨두면서도 LLM에게 전달되는 Context의 크기는 제한될 수 있다.

