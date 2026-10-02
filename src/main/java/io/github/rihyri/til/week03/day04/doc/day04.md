
<br>

# Week03 - Day 04. MultiModal & Local LLM

<br>

## 1. Multimodal

<br>

### 멀티모달이란?

멀티모달(Multimodal)은 텍스트뿐만 아니라 **이미지, 오디오, 비디오 등 서로 다른 형태의 데이터를 함께 입력받아 이해하고 추론하는 AI 기술**이다.

기존의 LLM이 주로 텍스트를 입력으로 사용했다면, 멀티모달 모델은 다양한 데이터를 함께 해석할 수 있다.

예를 들어 사용자가 사진과 함께

```azure
이 이미지에 어떤 문제가 있는지 알려줘.
```

라고 요청하면 AI는 텍스트와 이미지를 함께 분석하여 답변할 수 있다.

### 주요 기능

- 이미지 분석
- OCR을 통한 이미지 속 문자 인식
- 이미지 기반 시각적 추론
- 오디오 및 비디오 이해
- 공간 및 객체 관계 이해

<br>
<hr>

## 2. Spring AI에서 이미지 전달하기

<br>

Spring AI의 `ChatClient`에서는 사용자 메시지와 함께 이미지 등의 미디어 데이터를 전달할 수 있다.

```java
chatClient.prompt()
        .user(user -> user
    .text(message)
                .media(mimeType, image.getResource()))
    .call();
```

여기서 중요한 부분은 다음과 같다.

<br>

### .text()

사용자가 AI에게 전달할 질문이나 명령을 설정한다.

```java
.text("이 이미지를 분석해줘")
```

### .media()

AI가 분석할 이미지 파일을 함께 전달한다.

```java
.media(mimeType, image.getResource())
```

즉, 
```azure
사용자 질문 + 이미지 → ChatClient →  멀티모달 AI 모델 →  이미지 분석 결과 
```

의 흐름으로 동작한다.

<br>
<hr>

## 3. MultipartFile

<br>

웹에서 이미지를 업로드할 때 Spring에서는 일반적으로 `MultipartFile`을 사용한다.

```java
MultipartFile image
```

`MultipartFile`을 통해 다음 정보를 확인할 수 있다.

```java
image.getContentType(); // image/png, image/jpeg 등
image.getSize(); // 파일 크기
image.getResource(); // 실제 파일 Resuorce
```

이미지를 AI에게 전달하기 위해서는 파일뿐 아니라 `Content-Type` 정보도 함께 전달해야 한다.

예를 들어 PNG 이미지라면

```JAVA
image/png
```

와 같은 MIME type을 사용한다.

<br>
<hr>

## 4. ChatResponse와 Token Usage

<br>

단순히 AI의 문자열 응답만 필요한 경우에는 `.content()`를 사용할 수 있지만, 응답에 대한 추가 정보까지 필요한 경우 `chatResponse`를 사용할 수 있다.

```java
ChatResponse response = chatClient.prompt()
        ...
            .call()
        .chatResponse();
```

AI가 생성한 실제 답변은 다음과 같이 가져온다.

```java
response.getResult()
        .getOutput()
        .getText();
```

또한 응답의 메타데이터에서 토큰 사용량도 확인할 수 있다.

```java
Usage usage = response.getMetadata().getUsage();
```

대표적으로 다음 정보를 확인할 수 있다.

- Prompt Token : AI에게 전달한 입력 토큰
- Completion Token : AI가 생성한 출력 토큰
- Total Token : 전체 사용 토큰

토큰 사용량은 API 비용이나 프롬프트 크기를 확인할 때 유용하다.

<br>
<hr>

## 5. 멀티모달 활용 예시

<br>

이미지 분석은 단순히 이미지 설명 외에도 다양한 기능으로 활용할 수 있다.

<br>

### 영수증 분석

영수증 이미지에서

- 상호명
- 날짜
- 결제 금액
- 상품 목록

등을 추출할 수 있다.

특히 AI에게 JSON 형식으로 응답하도록 요청한 뒤 ObjectMapper를 이용하여 Java 객체로 변환할 수 있다.

```azure
영수증 이미지 → Multimodal AI → JSON 문자열 → ObjectMapper → ReceiptData 객체
```

그 외 명함 정보 추출, 제품 품질 검사에 이용할 수 있다.

<br>
<hr>

## 6. Local LLM

<br>

### 로컬 LLM이란?

ChatGPT나 Gemini와 같은 클라우드 AI API를 호출하는 것이 아니라, **내 컴퓨터나 서버에서 직접 LLM을 실행하는 방식**이다.

```azure
* Cloud LLM

Application 
     ↓ Internet
OpenAI / Gemini Server 
     

* Local LLM

Application → Local LLM Server → LLM Model
```

<br>
<hr>

## 7. Local LLM의 장단점

<br>

### 장점

- API 사용 비용을 줄일 수 있다.
- 데이터가 외부 서버로 전달되지 않아 보안에 유리하다.
- 인터넷이 없는 환경에서도 사용할 수 있다.
- API 호출 횟수 제한에서 비교적 자유롭다.

### 단점

- 충분한 CPU / GPU / RAM이 필요하다.
- 일반적으로 대형 클라우드 모델보다 성능이 낮을 수 있다.
- 모델 설치와 실행 환경을 직접 관리해야 한다.

따라서 민감한 데이터를 다루거나 내부망 환경, 프로토타입 개발 등에서는 Local LLM이 유용할 수 있다.

반대로 높은 추론 성능이나 빠른 서비스 출시가 중요하다면 클라우드 API가 더 적합할 수 있다.

<br>
<hr>

## 8. Ollama 

<br>

### Ollama란?

Ollama는 **LLM을 로컬 환경에서 쉽게 다운로드하고 실행할 수 있도록 도와주는 오픈소스 도구**이다.

복잡하게 직접 모델 실행 환경을 구성하지 않아도 명령어를 통해 모델을 실행할 수 있다.

예를 들어

```azure
ollama run qwen2.5:3b
```

처럼 모델을 실행할 수 있다.

Spring AI에서는 Ollama Starter를 사용하여 로컬에서 실행 중인 Ollama 서버와 연결할 수 있다.

```azure
Spring Boot → Spring AI ChatClient → Ollama Server → Qwen 등의 LLM
```

<br>
<hr>

## 9. LLM의 주요 생성 옵션

<br>

LLM은 다음 토큰을 확률적으로 선택하면서 문장을 생성한다.

예를 들어 다음 토큰 후보의 확률이

```azure
사과  40%
포도  30%
딸기  15%
바나나 10%
수박   5%
```

라고 가정할 수 있다.

이때 어떤 후보를 실제 선택 대상으로 사용할지를 조절하는 대표적인 옵션이 Temperature, Top-K, Top-P이다.

<br>

### Temperature

Temperature는 모델 답변의 **무작위성 또는 다양성**을 조절한다.

값이 낮으면 높은 확률의 토큰을 선택하는 경향이 강해진다.

```azure
낮은 Temperature → 일관되고 보수적인 답변
```

값이 높아지면 다양한 후보를 선택할 가능성이 증가한다.

```azure
높은 Temperature → 다양하고 창의적인 답변
```

정확한 정보 추출이나 분석에서는 낮은 값을 사용하는 경우가 많고, 아이디어 생성이나 창작에서는 상대적으로 높은 값을 사용할 수 있다.

### Top-K

Top-K는 **확률이 높은 상위 K개의 토큰만 후보로 남기는 방법**이다.

예를 들어

```azure
Top-K = 3

사과 40%
포도 30%
딸기 15%
----------------
바나나 10%  제외
수박 5%     제외
```

와 같이 상위 3개 후보만 다음 토큰 후보로 사용한다.

- 후보의 개수가 항상 K개로 고정된다.
- 지나치게 낮은 확률의 토큰을 제거할 수 있다.
- 값이 낮을수록 답변이 안정적이지만 다양성이 줄어들 수 있다.

### Top-P

Top-P는 **확률이 높은 토큰부터 더하여 누적 확률이 P에 도달할때 까지의 후보만 사용하는 방식**이다.

Nucleus Sampling이라고도 한다.

예를 들어

```azure
Top-P = 0.9

사과 50%
포도 25%
딸기 15%
바나나 7%
수박 3%

사과 50%
+ 포도 25%
+ 딸기 15%
= 90%
```

이므로 이 세 개의 토큰만 후보가 된다.

Top-K와 달리 후보 개수가 고정되어 있지 않다.

모델이 다음 단어를 확신하는 상황에서 후보가 적어질 수 있고, 확신하지 못하는 상황에서는 후보가 많아질 수 있다.

<br>
<hr>

## 10. Top-K와 Top-P 비교

<br>

<table>
    <thead>
        <th>구분</th>
        <th>Top-K</th>
        <th>Top-P</th>
    </thead>
    <tbody>
        <tr>
            <td>기준</td>
            <td>후보 개수</td>
            <td>누적 확률</td>
        </tr>
        <tr>
            <td>후보 수</td>
            <td>고정</td>
            <td>가변</td>
        </tr>
        <tr>
            <td>예</td>
            <td>상위 40개</td>
            <td>누적 확률 90%</td>
        </tr>
        <tr>
            <td>특징</td>
            <td>단순하고 명확</td>
            <td>문맥에 따라 유연하게 변화</td>
        </tr>
    </tbody>
</table>

<br>

중요한 점은 **Top-K나 Top-P는 무조건 높게 설정한다고 답변 품질이 좋아지는 옵션이 아니라는 것**이다.

값을 높이면 더 많은 토큰이 후보에 포함되기 때문에 답변의 다양성은 높아질 수 있지만, 불필요하거나 낮은 확률의 토큰도 선택될 가능성이 커진다.

반대로 너무 낮으면 안전한 토큰만 선택하게 되어 답변이 단조롭거나 반복적으로 변할 수 있다.

따라서 모델의 목적과 응답 특성에 맞게 조정해야 한다.

<br>
<hr>

## 정리

<br>

이번 수업에서는 기존의 텍스트 기반 LLM 호출에서 확장하여 이미지까지 함께 처리하는 **Multimodal AI**의 사용 방법을 학습했다.

Spring AI에서는 `chatClient`의 `media()`를 이용하여 이미지와 프롬프트를 함께 AI에게 전달할 수 있으며, 이를 활용하여 이미지 분석, OCR, 영수증 정보 추출 등 다양한 기능을 구현할 수 있다.

또한 Ollama를 통해 LLM을 로컬에서도 실행할 수 있다는 것을 배웠으며, LLM 응답 생성 과정에서 `Temperature, Top-K, Top-P`와 같은 옵션이 답변의 다양성과 안정성에 영향을 준다는 점을 이해하는 것이 중요하다.

<br>

