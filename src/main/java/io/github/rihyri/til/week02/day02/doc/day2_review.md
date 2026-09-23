# Day 2. 회고

<br>

이번에는 Validation부터 Service 계층의 역할, Builder 패턴, MapStruct까지 배웠다.

Validation 어노테이션 자체는 어렵지 않았지만 `@NotNull`, `@NotEmpty`, `@NotBlank`가 비슷해 보여서 처음에는 헷갈렸다. 특히 문자열에서는 공백까지 확인해주는 `@NotBlank`를 많이 사용한다는 점을 기억해두려고 한다.

가장 인상 깊었던 부분은 Service의 역할이었다. 이전에는 Service를 Controller와 Repository 사이에서 단순히 메서드를 호출해주는 곳이라고 생각했는데, **여러 작업을 묶어 하나의 비즈니스 행위를 만드는 계층**이라고 생각하니 더 이해가 됐다.

Lombok 역시 편하다고 해서 `@Data`나 `@Setter`를 무조건 사용하는 것이 좋은 것은 아니라는 점을 알게 됐다. 코드가 짧은 것보다 객체가 어디에서 어떻게 변경될 수 있는지를 명확하게 만드는 것이 더 중요해 보인다.

Builder와 MapStruct는 더 많이 사용해봐야 익숙해질 것 같다. 하지만 지금도 편리한 기능이라는 것은 알 수 있었다. service 계층까지 배웠으니 CRUD 연습을 해봐야겠다. 

