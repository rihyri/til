
<br>

# Week04 - Day 01. Embedding과 Vector DB

<br>

## 1. Embedding

<br>

LLM은 문자열 자체를 그대로 비교하는 것이 아니라, 텍스트의 의미를 숫자의 배열인 **Vector**로 표현할 수 있다.

이처럼 텍스트의 의미를 벡터로 변환하는 과정을 **Embedding**이라고 한다.

예를 들어 일반적인 문자열 검색에서 '강아지'와 '개'가 서로 다른 문자열이지만, 임베딩 공간에서는 의미가 비슷하기 때문에 서로 가까운 위치에 배치될 수 있다.

```aiignore
"강아지" → Embedding Model → [0.12, -0.43, 0.77, ...]
```

임베딩 벡터는 수백~수천 개의 차원을 가질 수 있으며, 각 숫자는 사람이 직접 정의한 하나의 의미라기보다는 모델이 학습한 여러 의미적 특징을 나타낸다.

<br>
<hr>

## 2. 벡터 간 유사도

<br>

두 텍스트가 얼마나 비슷한지는 각각의 임베딩 벡터 사이의 거리를 계산하여 판단한다.

대표적인 방법은 다음과 같다.

- Cosine Similarity / Cosine Distance
- Euclidean Distance
- Dot Product

이번 실습에서는 **Cosine Distance**를 사용한다.

```aiignore
사용자 질문 : "반품은 어떻게 하나요?"

           ↓ Embedding
            
    [0.11, 0.42, ...]
    
           ↓ 거리 비교
           
문서 A : "상품 반품은 배송 완료 후..." ← 가까움
문서 B : "회원 가입 방법은..." ← 멂
```

<br>
<hr>

## 3. Vector DB

<br>

Vector DB는 일반적인 문자열이나 숫자뿐만 아니라 **Embedding Vector를 저장하고 벡터 간 거리를 이용하여 검색할 수 있는 데이터베이스**이다.

일반 검색이 값의 일치 여부를 중심으로 찾는다면 Vector Search는 의미적으로 가까운 데이터를 찾는다.

```aiignore
* 일반 검색

WHERE content LIKE '%환불%'


* Vector Search

질문 Vector → Vector DB → 가장 가까운 Vector TOP 5
```

따라서 질문에 정확히 동일한 단어가 포함되지 않아도 의미가 비슷한 내용을 검색할 수 있다.

<br>
<hr>

## 4. pgvector 

<br>

pgvector는 PostgreSQL에서 Vector 데이터를 저장하고 검색할 수 있도록 해주는 확장 기술이다.

PostgreSQL을 그대로 사용하면서 다음과 같은 벡터 검색 기능을 추가할 수 있다.

```aiignore
CREATE EXTENSION IF NOT EXISTS vector;
```

이를 실행하면 PostgreSQL에서 다음과 같은 컬럼을 사용할 수 있다.

```aiignore
embedding vector(3072)
```

즉 하나의 행에 3072개의 숫자로 구성된 임베딩 벡터를 저장할 수 있다.

<br>
<hr>

## 5. 원본 문서와 Vector Store를 분리하는 이유

<br>

이번 실습에서는 두 개의 테이블을 사용한다.

```aiignore
* vector_documents

업로드된 원본 문서를 관리한다.
- 문서 ID
- 파일명
- 원본 내용
- 파일 타입
- 청크 개수


* vector_store

검색을 위해 분할된 문서 조각을 관리한다.
- 청크 내용
- Metadata
- Embedding Vector



구조는 다음과 같다.


vector_documents

[배송정책.txt]
        │
        ├── Chunk 1 → Vector
        ├── Chunk 2 → Vector
        └── Chunk 3 → Vector
        
                    ↓
                    
               vector_store
```

원본 문서는 관리 목적으로 보관하고, 검색은 작은 Chunk 단위로 수행한다.

<br>
<hr>

## 6. Chunk

<br>

긴 문서 전체를 하나의 벡터로 만들면 여러 주제가 하나의 벡터에 섞일 수 있다.

따라서 문서를 일정 크기로 잘라서 각각 임베딩한다.

```aiignore
"배송 정책 ... 환불 정책 ... 회원 정책 ..."

                ↓ Split

Chunk 1 "배송 정책 ... "

Chunk 2 "환불 정책 ... "

Chunk 3 "회원 정책 ... "                
```

Spring AI의 `TokenTextSplitter`를 이용하면 이를 자동으로 처리할 수 있다.

```java
TextSplitter splitter = TokenTextSplitter.builder()
        .withChunkSize(500)
        .build();
```

<br>
<hr>

## 7. Metadata

<br>

Vector 데이터에는 원문 외에도 Metadata를 함께 저장할 수 있다.

```aiignore
{
    "document_id": "문서 UUID",
    "filename": "policy.txt",
    "source": "user_upload"
}
```

Metadata를 이용하면 전체 Vector DB를 검색하지 않고 특정 문서 안에서만 검색할 수 있다.

예를 들어 

```aiignore
document_id = A
```

조건을 주면 A 문서에서 만들어진 Chunk만 대상으로 유사도 검색을 수행한다.

<br>
<hr>

## 8. 문서 업로드 전체 흐름

```aiignore
Controller
    │  file 업로드
    ▼
VectorDocumentService
    │
    ├─ 1. 파일 읽기
    │
    ├─ 2. 원본 문서 저장  
    │
    ├─ 3. TokenTextSplitter로 Chunk 분할
    │
    ├─ 4. Chunk에 Metadata 추가
    │
    ├─ 5. Embedding Model이 Vector 생성
    │
    └─ 6. pgvector에 저장
                │
                ▼
           vector_store
```

여기서 중요한 부분은 다음 코드이다.

```java
vectorStore.add(chunks);
```

코드에는 단순히 `add()`라고 되어 있지만 내부적으로 Spring AI가 Embedding Model을 호출하여 각 Chunk를 Vector로 변환한 뒤 Vector DB에 저장한다.

<br>
<hr>

## 9. 유사도 전체 흐름

<br>

검색 과정은 업로드 과정의 반대라고 생각하면 이해하기 쉽다.

```aiignore
사용자 질문

"배송은 며칠 걸리나요?" → Embedding Model → 질문의 Vector 생성 

→ pgvector에서 가장 가까운 Chunk 검색 → Top K 결과 반환
```

Spring AI에서는 다음 코드로 검색할 수 있다.

```java
vectorStore.similaritySearch(
        SearchRequest.builder()
                .query(query)
                .topK(3)
                .build()
);
```

query도 자동으로 임베딩된 후 저장된 Vector들과 비교된다.

<br>
<hr>

## 10. 핵심 정리

<br>

이번 실습에서 가장 중요한 흐름은 다음 하나이다.

```aiignore
[저장]

Document → Chunk → Embedding → Vector DB


[검색]

Question → Embedding → Vector DB 유사도 검색 → 가장 비슷한 Chunk
```

Vector DB 자체가 AI처럼 문장을 이해하는 것이 아니다.

**Embedding Model이 텍스트를 Vector로 변환하고, Vector DB는 그 Vector들의 거리를 계산하여 가까운 데이터를 찾아주는 역할을 한다.**

<br>