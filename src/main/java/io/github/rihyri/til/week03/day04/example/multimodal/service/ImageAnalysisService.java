package io.github.rihyri.til.week03.day04.example.multimodal.service;

import io.github.rihyri.til.week03.day01.example.ai.exception.AiServiceException;
import io.github.rihyri.til.week03.day04.example.exception.DomainException;
import io.github.rihyri.til.week03.day04.example.exception.DomainExceptionCode;
import io.github.rihyri.til.week03.day04.example.multimodal.dto.ImageAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final ChatClient chatClient;

    public ImageAnalysisResponse analyze (String message, MultipartFile image) throws Exception {

        /*
         *  1. 사용자가 업로드한 파일의 Content-Type을 가져온다.
         *
         *  PNG 파일이면 -> image/png, JPEG 파일이면 -> image/jped
         *
         *  AI에게 이미지를 전달할 때 이미지가 어떤 형식인지 알려주기 위해 필요하다.
         */
        String contentType = image.getContentType();

        /*
         *  Content-Type을 확인할 수 없는 경우를 대비한 기본값과
         *  실제로는 "지원하지 않는 이미지 형식입니다."와 같은 예외를 발생시키는 방법도 사용할 수 있다.
         */
        if (contentType == null) {
            contentType = "image/jpeg";
        }

        /*
         *  문자열로 전달받은 MIME type을 Spring에서 사용하는 MimeType 객체로 변환한다.
         */
        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);

        try {
            /*
             *  2. AI에게 요청을 전달한다.
             *
             *  기존 텍스트 채팅과 가장 큰 차이점은 user() 내부에서
             *  text()와 media()를 동시에 전달한다는 것이다.
             */
            var response = chatClient.prompt()
                    // 사용자 메시지 구성
                .user(user -> user
                    // AI에게 전달할 질문
                    .text(message)
                    /*
                     *  이미지 전달
                     *
                     *  첫 번째 값 : 이미지 MIME type
                     *  두 번째 값 : 실제 이미지 Resuorce
                     *
                     *  즉, AI에게 [텍스트 질문 + 이미지]를 동시에 전달하는 부분이다.
                     */
                    .media(
                        mimeType, image.getResource()
                    )
                )
                .call()
                .chatResponse();

            /*
             *  3. AI가 생성한 실제 답변을 꺼낸다.
             */
            String result = response
                .getResult()
                .getOutput()
                .getText();

            /*
             *  4. 응답 Metadata에서 토큰 사용량을 가져온다.
             */
            Usage usage = response.getMetadata().getUsage();

            ImageAnalysisResponse.TokenUsage tokenUsage = null;

            if (usage != null) {
                tokenUsage =
                    ImageAnalysisResponse.TokenUsage.builder()
                        .promptToken(usage.getPromptTokens())
                        .completionTokens(usage.getCompletionTokens())
                        .totalTokens(usage.getTotalTokens())
                        .build();
            }

            /*
             *  5. Controller에서 반환할 최종 Response DTO 생성
             */
            return ImageAnalysisResponse.builder()
                .result(result)
                .contentType(contentType)
                .fileSize(image.getSize())
                .tokenUsage(tokenUsage)
                .build();
        } catch (Exception e) {
            log.error("이미지 분석 중 오류가 발생했습니다. message={}", e.getMessage(), e);
            throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
        }
    }
}
