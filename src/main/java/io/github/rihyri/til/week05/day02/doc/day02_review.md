<br>

Week 05 - Day 02 : 학습 회고

<br>

이번 수업에서는 인메모리 저장소의 개념과 Redis의 특징을 학습하고, Spring Boot에서 Redis를 활용하는 방법을 실습했다.

특히 Spring Session을 이용한 세션 저장과 Redis의 다섯 가지 기본 자료구조를 살펴보았다.

새롭게 이해한 부분은 다음과 같다.

<br>

### 1. Redis는 단순한 캐시 저장소 장소가 아니다.

처음에는 Redis를 빠르게 데이터를 저장하고 조회하는 용도로만 생각했지만, List, Set, Hash, Sorted Set 등 다양한 자료구조를 지원한다는 것을 알게 되었다.

특히 Sorted Set을 이용하면 점수에 따른 순위를 관리할 수 있어 실시간 랭킹 기능에 적합하다는 점이 흥미로웠다.

### 2. Spring Session을 사용하면 HttpSession의 저장 위치를 변경할 수 있다.

Controller에서 기존처럼 `session.setAttribute()`를 사용하더라도, Spring Session을 연동하면 세션 정보가 Redis에 저장될 수 있다는 점을 이해했다.

개발자가 모든 Redis 저장 로직을 직접 구현할 필요가 없다는 것이 인상적이었다.

### 3. RedisTemplate은 자료구조에 따라 메서드가 구분된다.

`opsForValue`, `opsForList()`, `opsForSet()` 등 자료구조별로 메서드가 나뉘어 있다는 점을 배웠다.

각 메서드가 어떤 Redis 명령어와 연결되는지 이해하는 것이 중요하다고 느꼈다.

<br>

Redis의 자료구조마다 데이터를 저장하고 조회하는 방식이 달라 처음에는 메서드를 구분하기 어려웠다.

특히 List의 왼쪽과 오른쪽 삽입 및 삭제 방식, Sorted Set의 오름차순-내림차순 순위 조회가 헷갈렸다.

또한 RedisTemplate의 직렬화 설정과 Spring Session의 세션 직렬화 설정이 서로 별개라는 점도 추가로 정리할 필요가 있다.

<br>

이번 학습을 통해 Redis가 단순히 임시 저장소를 넘어 세션 관리, 집합 연산, 랭킹 등 다양한 기능에 활용할 수 있다는 점을 알게 되었다.

앞으로는 데이터를 무조건 Redis에 저장하기보다, 데이터의 특성과 저장 목적에 맞는 자료구조를 선택하는 연습이 필요할 것 같다.

<br>

