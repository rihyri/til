<br>

# Week 4 - Day 3. 회고

<br>

이번 수업에서는 Spring AI의 `Advisor`와 `Function Calling`을 학습했다.

처음에는 Advisor를 사용하려면 Service에서 `ChatClient`를 호출할 때마다 Advisor 관련 코드를 추가해야 하는 것으로 이해했다.

하지만 Advisor를 구현한 뒤 ChatClient의 `defaultAdvisors()`에 등록하면, 이후 Service에서는 일반적인 방식으로 ChatClient를 호출할 수 있다는 점을 알게 되었다.

특히 BaseAdvisor를 통해 `before()`와 `after()`를 구분하고, 요청 전후에 서로 다른 작업을 수행할 수 있다는 점이 인상 깊었다.

`ChatMemoryAdvisor`를 직접 구현하면서는 Spring AI보다 Java 문법이 더 어렵게 느껴지는 부분도 있었다.

`CopyOnWriteArrayList`, `computeIfAbsent()`, `Optional` 등의 코드를 이해하면서 자료구조와 람다식, Builder 패턴에 대해서도 다시 공부할 수 있었다.

`Function Calling`에서는 LLM이 직접 Java 코드를 실행하는 것이 아니라, 필요한 Tool을 선택하고 Spring AI가 이를 실행해 결과를 전달한다는 구조를 이해했다.

앞으로는 Advisor를 활용해 공통 기능을 분리하고, Function Calling을 통해 실제 외부 API를 호출하는 예제를 구현해봐야겠다.

**오늘의 핵심은 ChatClient의 기본 동작을 확장하는 Advisor와, 애플리케이션의 기능을 LLM에 제공하는 Function Calling의 역할을 구분하는 것이다.**

<br>

