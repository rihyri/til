package io.github.rihyri.til.week03.day04.example.multimodal.controller;

import io.github.rihyri.til.week03.day04.example.multimodal.dto.ImageAnalysisResponse;
import io.github.rihyri.til.week03.day04.example.multimodal.service.ImageAnalysisService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageAnalysisService imageAnalysisService;

    /*
     *  일반 JSON 요청이 아니라 이미지 파일을 함께 전달하기 때문에
     *  multipart/from-data 방식으로 요청받는다.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageAnalysisResponse analyze (@RequestParam String message, @RequestParam MultipartFile image)
        throws Exception {

        return imageAnalysisService.analyze(message, image);
    }
}
