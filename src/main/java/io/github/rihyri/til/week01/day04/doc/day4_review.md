# Week 01 - Day 4. 회고

<br>

오늘은 자기 참조를 이용한 계층형 데이터 구조와 DTO를 이용해 Entity를 응답 객체로 변환하는 방법을 배웠다. 또한 JPA의 로딩 전략과 N+1, Fetch Join과 BatchSize에 대해서도 공부했다.

DTO 자체는 이전에도 사용해봤지만 DTO 내부에 여러 static inner class를 만들고, Service에서 `stream()`, `map()`, `toList()`를 이용해 Entity를 DTO로 변환하는 코드는 처음이라 이해하기 어려웠다. 특히 `map()` 안에서 Builder까지 사용하니 더 헷갈리게 느껴졌다.

따라서 Stream 코드를 바로 이해하려고 하기보다 먼저 일반 `for`문으로 바꿔서 생각해보니 조금 이해하기 쉬웠다.

```aiignore
List<Entity> → Entity를 하나씩 꺼냄 → DTO로 변환 → List<DTO>
```

과정을 `stream().map().toList()`로 표현한 것이었다.

DTO 변환 코드는 아직 익숙하지 않기 때문에 간단한 Entity를 직접 만들어 연습을 해봐야겠다.
지연로딩과 N+1 문제에 대하여 더 알 수 있는 계기가 되었다. 

--

첫 주 수업이 끝났다! 

처음에는 3시간씩 앉아 있는 것이 힘들었지만 어느새 서서히 적응이 되기 시작했다.

토요일까지 잘 참여하여 첫 주를 보람차게 보낼 수 있도록 해야겠다~!