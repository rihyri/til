<br>

# Week 04 - Day 02. RAG

<br>

## 1. RAG란?

<br>

RAG(Retrieval-Augmented Generation)는 **외부 데이터에서 필요한 정보를 검색한 뒤, 검색 결과를 LLM에게 함께 전달하여 답변을 생성하는 방식**이다.

일반적인 LLM은 학습 당시의 데이터만 알고 있기 때문에 다음과 같은 한계가 있다.

- 최신 정보를 알지 못할 수 있다.
- 회사 내부 문서와 같은 비공개 데이터를 알 수 없다.
- 모르는 내용도 그럴듯하게 만들어내는 환각(Hallucination)이 발생할 수 있다.

RAG는 이런 문제를 줄이기 위해 외부 문서를 먼저 검색하고, 검색된 내용을 근거로 답변을 생성한다.

<br>
<hr>

## 2. RAG 기본 흐름

<br>

```aiignore
사용자 질문 → 질문 Embedding → Vector Store 유사도 검색 → 관련 Document 검색 

→ Document 내용을 Prompt에 추가 → LLM → 최종 답변 
```

핵심은 **LLM이 바로 답변하지 않는다는 것**이다.

먼저 Vector Store에서 질문과 의미적으로 가까운 문서를 찾고, 그 문서를 LLM에게 Context로 제공한 뒤 답변을 생성한다.

<br>
<hr>

## 3. RAG와 Fine-tuning

<br>

<table>
    <thead>
        <th>구분</th>
        <th>RAG</th>
        <th>Fine-tuning</th>
    </thead>
    <tbody>
        <tr>
            <td>지식 반영</td>
            <td>외부 DB 검색</td>
            <td>모델 파라미터 학습</td>
        </tr>
        <tr>
            <td>최신 데이터</td>
            <td>DB만 변경하면 반영 가능</td>
            <td>다시 학습해야 함</td>
        </tr>
        <tr>
            <td>비용</td>
            <td>상대적으로 낮음</td>
            <td>상대적으로 높음</td>
        </tr>
        <tr>
            <td>출처 확인</td>
            <td>가능</td>
            <td>어려움</td>
        </tr>
        <tr>
            <td>주요 목적</td>
            <td>특정 문서 기반 답변</td>
            <td>말투, 형식, 도메인 특성 학습</td>
        </tr>
    </tbody>
</table>

RAG는 **지식을 추가하는 것**,
Fine-tuning은 **모델의 행동이나 답변 스타일 자체를 학습시키는 것**에 더 가깝다.

<br>
<hr>

## 4. Similarity Search

<br>

Vector Store에서는 질문과 의미적으로 비슷한 문서를 찾기 위해 유사도 검색을 사용한다.

```java
SearchRequest.builder()
    .query(query)
    .topK(topK)
    .similarityThreshold(threshold)
    .build();
```

<br>

### query
검색할 사용자 질문이다.

### topK
검색 결과 중 최대 몇 개의 문서를 가져올 것인지 지정한다.
예를 들어 

```java
topK = 5
```

이면 최대 5개의 Document를 반환한다.

### similarityThreshold
검색 결과로 인정할 최소 유사도 기준이다.

Threshold가 너무 낮으면 질문과 관계없는 문서까지 검색될 수 있고, 너무 높으면 필요한 문서도 검색하지 못할 수 있다.

따라서 RAG에서는 topK와 similarityThreshold를 적절하게 조정하는 것이 중요하다.

<br>
<hr>

## 5. RAG API 종류

<br>

이번 실습에서는 하나의 RAG 기능을 여러 API로 나누었다.

### /ask
```aiignore
질문 
    → 관련 문서 검색
    → 문서를 이용해 LLM 답변 생성
    → 답변만 반환
```

가장 기본적인 RAG API이다.

### /ask-with-source

```aiignore
질문
    → 관련 문서 검색
    → LLM 답변 생성
    → 답변 + 참고 문서 정보 반환
```

/ask와 답변 생성 과정은 같지만 사용자에게 어떤 문서를 참고했는지도 함께 보여준다.

### /ask-in-document/{documentId}
```aiignore
질문 + documentId
    → 해당 documentId 문서만 대상으로 검색
    → LLM 답변 생성
```

모든 문서에서 검색하지 않고 **특정 문서 내부에서만 검색**한다.

예를 들어 사용자가 업로드한 특정 매뉴얼 하나에 대해서만 질문하게 만들 때 사용할 수 있다.

### /search

```aiignore
질문 
    → Vector Store 검색
    → 검색 결과 그대로 반환
```

LLM을 사용하지 않는다.

RAG는 **Retrieval 단계만 확인하기 위한 API**라고 볼 수 있다.

검색 결과에는 Document의 내용과 Metadata 등이 포함될 수 있기 때문에 검색 품질을 확인하거나 디버깅할 때 유용하다.

### /search-summary

```aiignore
검색어
    → 관련 문서 검색
    → 검색된 문서들을 LLM에게 전달
    → 한 문장 요약
```

검색 결과를 그대로 반환하지 않고 LLM을 이용하여 요약한다.

<br>
<hr>

## 6. Metadata

<br>

Vector Store에서 가져온 Document에는 본문뿐 아니라 Metadata도 존재한다.

예를 들어

```aiignore
filename
document_id
chunk_index
source
```

등의 부가 정보를 저장할 수 있다.

이를 이용하면

```java
doc.getMetadata().get("filename")
```

처럼 원본 파일명을 가져오거나

```java
.filterExpression("document_id =='" + documentId + "'")
```

처럼 특정 문서만 검색하도록 제한할 수 있다.

<br>
<hr>

## 7. 문서 여러 개를 하나의 Context로 만들기

<br>

Vector Store에서는 여러 개의 Document가 검색될 수 있다.

LLM에게 전달하려면 여러 문서의 내용을 하나의 문자열로 합쳐야 한다.

```java
documents.stream()
    .map(...)
    .collect(Collectors.joining("\n\n==\n\n"));
```

결과적으로 다음과 같은 Context가 만들어진다.

```aiignore
[guide1.txt]: 첫 번째 문서 내용 

==

[guide2.txt]: 두 번째 문서 내용
```

이 Context와 사용자 질문을 함께 Prompt에 넣어 LLM에게 전달한다.

<br>
<hr>

## 정리

<br>

이번 실습에서 중요한 흐름은 다음과 같다.

```aiignore
Question → VectorStore → Similarity Search → Context 생성 

→ Prompt + Context + Question → LLM → Answer 
```

RAG에서 LLM만큼 중요한 것이 **Retrieval** 이다.

잘못된 문서를 검색하면 아무리 좋은 LLM을 사용해도 정확한 답변을 만들기 어렵다.

따라서 `topK`, `similarityThreshold`, `Metadata Filter` 등을 이용해 **질문과 관련된 문서를 얼마나 잘 검색하는지가 RAG 품질에 큰 영향을 준다.**

<br>