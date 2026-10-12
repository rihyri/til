<br>

# Week 4. Spring AI - Vector DB, RAG & Advisor

<br>

4주차에는 `Embedding`과 `Vector DB`를 이용한 의미 기반 검색부터, `RAG`, `Chat Memory`, `Function Calling`까지 학습했다.

3주차가 LLM의 대화 Context를 저장하고 관리하는 과정이었다면, 4주차는 **외부 데이터를 검색하여 LLM에 제공하고, AI 호출 과정에 필요한 기능을 확장하는 방법**에 집중했다.

전체적인 흐름은 다음과 같다.

```aiignore
사용자 질문 → Advisor → 관련 데이터 검색 (RAG) → LLM → 필요 시 Tool 실행 → 최종 응답
```

<br>
<hr>

## 1. Embedding과 Vector DB

<br>

### Embedding

Embedding은 텍스트의 의미를 숫자 배열인 Vector로 변환하는 기술이다.

일반적인 문자열 검색과 달리, 표현이 서로 달라도 의미가 비슷한 텍스트를 찾을 수 있다.

```aiignore
텍스트 → Embedding Model → Vector
```

변환된 벡터 간의 유사도를 측정하는 대표적인 방법은 다음과 같다.

- Cosine Similarity / Distance
- Euclidean Distance
- Dot Product

<br>

### Vector DB

Vector DB는 Embedding Vector를 저장하고 유사도 검색을 수행하는 데이터베이스이다.

이번 실습에서는 PostgreSQL의 `pgvector` 확장을 활용했다.

```aiignore
사용자 질문 → Embedding → Vector DB → 유사한 문서 검색
```

<br>

### Chunk와 Metadata

긴 문서를 효율적으로 검색하기 위해 작은 단위인 Chunk로 분할한다.

- **Chunk** : 원본 문서를 검색하기 적절한 크기로 나눈 조각
- **Metadata** : 문서 ID, 파일명, 출처 등 부가 정보

원본 문서는 별도로 보관하고, 검색용 Chunk와 Embedding Vector는 Vector Store에서 관리할 수 있다.

이를 통해 전체 문서가 아닌 질문과 관련된 부분만 검색할 수 있다.

<br>
<hr>

## 2. RAG (Retrieval-Augmented Generation)

<br>

RAG는 외부 데이터를 검색한 뒤, 검색 결과를 LLM의 Context에 추가하여 답변을 생성하는 방식이다.

LLM이 학습하지 않는 내부 문서나 최신 정보를 활용할 수 있으며, 검색한 자료를 근거로 답변하도록 유도해 Hallucination을 줄이는 데 도움을 준다.

<br>

### RAG 동작 과정

```aiignore
사용자 질문 → 질문 Embedding → Vector Store 검색 → 관련 Document 조회

→ Prompt에 추가 → LLM 답변 생성
```

RAG에서 중요한 것은 **Retrieval(검색)의 품질이 최종 답변의 품질에 영향을 준다는 점**이다.

<br>

### Similarity Search

Vector Store에서 문서를 검색할 때 다음 설정을 활용할 수 있다.

<table>
    <thead>
        <th>설정</th>
        <th>역할</th>
    </thead>    
    <tbody>
        <tr>
            <td>query</td>
            <td>검색할 질문</td>
        </tr>
        <tr>
            <td>topK</td>
            <td>가져올 문서의 최대 개수</td>
        </tr>
        <tr>
            <td>similarityThreshold</td>
            <td>검색 결과의 최소 유사도 기준</td>
        </tr>
        <tr>
            <td>filterExpression</td>
            <td>Metadata 기반 검색 범위 제한</td>
        </tr>
    </tbody>
</table>

관련 없는 문서가 Context에 포함되지 않도록 검색 범위와 기준을 적절히 설정하는 것이 중요하다.

<br>
<hr>

## 3. Spring AI Advisor

<br>

Advisor는 `ChatClient`의 요청과 응답을 처리하는 과정에서 공통 기능을 추가하는 확장 기능이다.

대표적인 기능은 다음과 같다.

- Chat Memory를 이용한 대화 기록 관리
- RAG 검색 결과를 Prompt에 추가
- 요청 및 응답 로깅
- 입력 검증 및 응답 후처리

<br>

### Advisor Chain

여러 Advisor를 등록하면 체인 형태로 실행된다.

```aiignore
요청 → Advisor A → Advisor B → LLM → Advisor B → Advisor A → 응답
```

`getOrder()`로 실행 순서를 지정할 수 있으며, 요청과 응답 처리 과정에서 공통 기능을 분리할 수 있다.

또한 ChatClient 생성 시 `defaultAdvisors()`에 등록하면 Service에서 매번 Advisor를 지정하지 않아도 된다.

즉, Service는 LLM 호출에 집중하고 부가 기능은 Advisor가 담당하도록 역할을 분리할 수 있다. 

<br>
<hr>

## 4. Chat Memory

<br>

LLM은 이전 API 요청의 대화를 자동으로 기억하지 않는다.

Chat Memory는 이전 메시지를 저장하고 새로운 질문과 함께 전달하여 연속적인 대화를 지원한다.

Spring AI에서는 다음 구성요소를 활용할 수 있다.

<table>
    <thead>
        <th>구성요소</th>
        <th>역할</th>
    </thead>    
    <tbody>
        <tr>
            <td>ChatMemory</td>
            <td>대화 메시지 및 유지 범위 관리</td>
        </tr>
        <tr>
            <td>ChatMemoryRepository</td>
            <td>대화 메시지 저장 및 조회</td>
        </tr>
        <tr>
            <td>MessageChatMemoryAdvisor</td>
            <td>ChatClient와 Chat Memory 연결</td>
        </tr>
        <tr>
            <td>MessageWindowChatMemory</td>
            <td>최근 메시지 중심으로 대화 유지</td>
        </tr>
    </tbody>
</table>

`conversation ID`를 이용해 대화방별 기록을 분리할 수 있다.

여기서 중요한 점은 **Chat History와 Chat Memory의 목적이 다르다는 것**이다.

Chat History는 전체 대화 기록을 보관하는 데 목적이 있고, Chat Memory는 LLM에게 필요한 대화 문맥을 제공하는 데 목적이 있다.

<br>
<hr>

## 5. Function Calling

<br>

Function Calling은 LLM이 답변을 생성하는 과정에서 애플리케이션의 외부 기능을 활용할 수 있도록 하는 기술이다.

예를 들어 상품 조회, 데이터베이스 검색, 외부 API 호출 등의 작업에 사용할 수 있다.

<br>

### 동작 과정

```aiignore
사용자 질문 → LLM이 Tool 호출 판단 → Tool 이름 및 인자 생성 → 애플리케이션에서 실행

→ 결과를 LLM에 전달 → 최종 응답 
```

여기서 핵심은 **LLM이 Java 메서드를 직접 실행하는 것이 아니라, 실행할 Tool과 인자를 결정한다는 것**이다.

실제 메서드 실행은 Spring AI 애플리케이션에서 담당한다.

Spring AI에서는 `@Tool`을 통해 Java 메서드를 Tool로 정의할 수 있다.

- defaultTools() : ChatClient에 기본 Tool 등록
- tools() : 개별 요청에 Tool 지정

Tool은 LLM이 사용할 수 있도록 애플리케이션에서 제공한 기능일 뿐, 등록했다고 반드시 호출되는 것은 아니다.

또한 Tool이 DB 변경이나 외부 작업을 수행할 수 있으므로 권한 검증과 안전한 실행 범위 설정이 필요하다.

<br>
<hr>

## 4주차 핵심 흐름

<br>

이번 주에 학습한 기술들을 하나의 AI 서비스에 연결하면 다음과 같다.

### 1. 문서 저장

```aiignore
원본 문서 → Chunk 분할 → Embedding → Vector Store 저장
```

### 2. 사용자 질문 처리

```aiignore
사용자 질문 → Advisor → Chat Memory 조회 및 RAG 검색 → Context 구성 → LLM 호출
```

### 3. 외부 기능 실행

```aiignore
LLM이 Tool 필요 여부 판단 → Function Calling → 실행 결과 전달 → 최종 답변
```

이를 통해 AI 서비스는 단순히 질문에 답하는 것을 넘어, 외부 문서를 참고하고 이전 대화를 유지하며 애플리케이션의 기능까지 활용할 수 있다.

<br>
<hr>

## 마치며

<br>

3주차에는 LLM이 이전 대화를 기록하도록 Context를 구성하고 관리하는 방법을 학습했다.

4주차에는 여기서 한 단계 더 나아가 외부 문서를 검색하고, 필요한 정보를 Prompt에 추가하며, Advisor와 Function Calling을 통해 AI 기능을 확장하는 방법을 학습했다.

이번 주 학습을 통해 다음 기술들이 서로 독립적인 기능이 아니라 하나의 AI 서비스 안에서 연결된다는 점을 이해할 수 있었다.

```aiignore
Embedding → Vector Search → RAG → Advisor → Function Calling
```

4주차의 핵심은 **LLM에게 필요한 정보를 어떻게 찾아서 전달하고, AI 호출 과정과 외부 기능을 어떻게 연결할 것인가?**라는 질문으로 정리할 수 있다.

<br>

