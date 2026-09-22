# Day 1. 회고

<br>

2주차 1일은 Spring에서 객체를 직접 생성하지 않고 IoC와 DI를 통해 객체를 관리하는 과정부터 Controller에서 HTTP 요청을 처리하는 방법까지 학습했다.

`PathVariable`, `@RequestBody`, `@RequestParam`처럼 요청 값을 받는 방법은 이전에도 본 적이 있지만 각각 언제 사용하는지 정확하게 구분하지 못했는데, REST API의 요청 형태와 함께 보니 조금 더 이해가 되었다.

특히 `ApiResponse`, `GlobalExceptionHandler`, `DomainException` 처럼 응답과 예외 처리를 위해 여러 클래스를 나누는 부분이 처음에는 복잡하게 느껴졌다. 하지만 각각 **응답 형식 정의, 예외 처리, 도메인 예외표현**이라는 서로 다른 역할을 담당한다는 것을 알게 되었다.

아직 예외 처리 구조를 처음부터 작성하기는 어려울 것 같지만, 코드를 보았을 때 요청이 Controller로 들어와 처리되고 예외가 발생되면 GlobalExceptionHandler를 거쳐 공통 응답으로 반환되는 흐름은 이해할 수 있도록 복습해야겠다.

<br>

