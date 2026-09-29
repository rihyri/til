<br>

# Day 1. 회고

<br>

이번에는 AI와 LLM의 기본 개념을 학습하고, Spring AI를 활용하여 Spring Boot 애플리케이션에서 LLM API를 호출하는 방법을 배웠다.

기존에 Controller와 Service를 통해 데이터베이스에 접근했던 것처럼, Spring AI에서는 `ChatClient`를 통해 외부 AI 모델과 통신할 수 있다는 점이 흥미로웠다.

특히 Token과 Embedding의 개념을 학습하면서 LLM이 문장을 처리하는 과정과 RAG에서 벡터 검색이 사용된다는 것도 알게되었다.

<br>

가장 헷갈렸던 부분은 `ChatClient.Builder`와 `ChatClient`의 차이였다.

처음 예제에서는 직접 생성자를 작성했지만, 이후 예제에서는 `@RequiredArgsConstructor`를 사용해서 두 방식의 차이가 명확하지 않았다.

또한 `prompt(), user(), call(), content()`가 각각 어떤 역할을 수행하는지 처음에는 이해하기 어려웠다.

`ChatClient.Builder`는 `ChatClient`를 생성하기 위한 객체이고, `build()`를 호출하면 실제 사용할 `ChatClient`가 만들어진다는 것을 알게 되었다.

AI API도 기존 백엔드 구조처럼 Controller와 Service를 분리하고 DTO를 통해 요청과 응답을 관리하는 것이 유지보수에 유리하다는 점을 이해했다.

<br>

AI 기술은 빠르게 변화하지만, 결국 백엔드에서 중요한 것은 요청과 응답의 흐름, 의존성 주입, 예외 처리, 보안이라는 생각이 들었다.

아직 ChatClient 사용법이 익숙하지 않지만, 기존에 배운 Spring 구조와 연결하면 잘 이해할 수 있을 것 같다.

<br>

