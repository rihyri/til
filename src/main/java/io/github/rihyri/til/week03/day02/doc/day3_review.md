<br>

# Week 3 - Day 2. 회고

<br>

Spring AI의 ChatClient를 활용하여 LLM 통신하는 방법을 학습했다.

System, User, Assistant Message의 역할을 구분하고, 이전 대화 내용을 직접 저장하여 연속적인 대화를 구현하는 방법을 배웠다.

또한 Context Window와 Token Usage를 이해하고, 대화 히스토리가 길어질수록 토큰 사용량과 비용이 증가할 수 있다는 점을 알게 되었다.

실시간 AI응답을 위한 Flux와 여러 사용자의 대화 기록을 관리하기 위한 ConcurrentHashMap도 접할 수 있었다.

<br>

이번 수업에서는 Service 코드가 특히 어려웠다.

기존에는 Service에서 Repository를 호출하고 데이터를 조회하거나 저장하는 로직을 주로 작성했는데, 이번에는 외부 AI API를 호출하면서 대화 기록까지 직접 관리해야 했기 때문이다.

특히 다음 내용이 어려웠다.

- ConcurrentHashMap을 사용하는 이유와 스레드의 개념
- conversationId를 이용하여 사용자별 대화 기록을 관리하는 방법
- List<Message>에 UserMessage와 AssistantMessage를 저장하는 과정
- Flux를 반환하는 스트리밍 응답의 동작 방식
- ChatResponse에서 답변과 토큰 사용량을 추출하는 과정

<br>

LLM은 이전 대화를 자동으로 기억하는 것이 아니라, 애플리케이션에서 이전 메시지를 저장하고 다음 요청에 함께 전달해야 한다는 점이 인상적이었다.

또한 ConcurrentHashMap을 통해 여러 세르드가 공유하는 데이터를 관리할 수 있지만, 내부에 저장된 ArrayList까지 자동으로 안전해지는 것은 아니라는 점을 이해했다.

Flux는 아직 이국하지 않지만, 완성된 응답 하나를 반환하는 방식과 달리 데이터를 여러 조각으로 전달하는 스트리밍 타입이라는 점을 알게 되었다.

<br>

이번 수업부터 AI API를 단순히 호출하는 것을 넘어, 실제 서비스에서 대화 상태와 비용을 관리하는 방법을 배우기 시작했다.

처음 보는 개념이 많아서 어려웠지만, 기존에 학습했던 Controller와 Service의 역할을 연결해서 생각하니 전체적인 구조를 조금씩 이해할 수 있었다.

<br>