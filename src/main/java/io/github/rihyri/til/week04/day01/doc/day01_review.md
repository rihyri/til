<br>

# Week 4 - Day 1. 회고

<br>

이번 수업에서는 Embeding과 Vector DB의 개념, 그리고 PostgreSQL의 pgvector를 이용하여 문서를 저장하고 유사도 검색을 수행하는 방법을 학습했다.

<br>

Embedding과 Vector DB 자체의 개념은 크게 어렵지 않았다. 문자열을 그대로 비교하는 것이 아니라 텍스트를 Vector로 변환한 뒤 Vector 사이의 거리를 이용하여 의미적으로 비슷한 내용을 검색한다는 점을 쉽게 이해할 수 있다.

반면 실제 코드에서는 `PgVectorStore`, `EmbeddingModel`, `TokenTestSplitter`, `Document`, `SearchRequest` 등 처음 보는 클래스가 많이 등장하여 전체 흐름을 이해하기 어려웠다. 특히 `vectorStore.add()`만 호출했는데 어디에서 Embedding이 생성되고 Vector DB에 저장되는지 처음에는 명확하게 보이지 않았다.

코드를 다시 살펴보면서 문서 업로드 과정이 **원본 저장 → Chunk 분할 → Embedding 생성 → Vector DB 저장**순서로 진행되고, 검색 과정은 **질문 Embedding → Vector 간 거리 계산 → 유사한 Chunk 검색** 순서로 진행된다는 것을 이해했다.

또한 원본 문서를 저장하는 `vector_documents`와 검색용 Chunk를 저장하는 `vector_store`의 역할이 서로 다르며, Metadata의 `document_id`를 이용하면 특정 문서에 속한 Chunk만 검색할 수 있다는 점도 알게 되었다.

<br>

Spring AI가 Embedding 생성과 Vector DB 연동 과정의 많은 부분을 추상화해주기 때문에 코드만 처음 보면 내부 흐름을 이해하기 어려운 것 같다. 

앞으로는 새로운 API를 볼 때 메서드 자체를 외우기보다 해당 객체가 전체 흐름에서 어떤 역할을 담당하는지 먼저 파악하는 방식으로 공부해야겠다.

<br>