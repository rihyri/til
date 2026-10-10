package io.github.rihyri.til.week04.day03.example.tool;

import io.github.rihyri.til.week04.day03.example.dto.CalculatorRequest;
import io.github.rihyri.til.week04.day03.example.dto.CalculatorResponse;
import io.github.rihyri.til.week04.day03.example.dto.CurrentTimeResponse;
import io.github.rihyri.til.week04.day03.example.dto.WeatherRequest;
import io.github.rihyri.til.week04.day03.example.dto.WeatherResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FunctionTools {

    private static final Map<String, Integer> TEMPERATURES =
        Map.of(
            "서울", 18,
            "부산", 22,
            "대구", 20,
            "제주", 24
        );

    /*
     *  1. 날씨 조회
     *
     *  - @Tool : Spring AI에게 해당 메서드가 AI가 호출할 수 있는 Tool임을 알려준다.
     *  - description : LLM이 해당 도구가 어떤 기능을 하는지 판단할 수 있도록 제공하는 설명이다.
     */
    @Tool(description =
        "학습용 가상 날씨 데이터를 조회합니다."
        + "서울, 부산, 대구, 제주의 예시 날짜를 제공합니다."
        + "실시간 날씨 정보가 아닙니다.")
    public WeatherResponse getWeather (WeatherRequest request) {

        log.info("[Weather Tool] city : {}", request.getCity());

        String city = request.getCity();

        int temperature = TEMPERATURES.getOrDefault(city, 15);

        String condition = switch (city) {
            case "서울" -> "맑음";
            case "부산" -> "구름 많음";
            case "대구" -> "흐림";
            case "제주" -> "비";
            default -> "정보 없음";
        };

        return WeatherResponse.builder()
            .city(city)
            .temperature(temperature)
            .condition(condition)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /*
     *  2. 계산기 Tool
     *
     *  LLM이 수학 계산을 직접 하는 대신 Java 메서드를 호출하여 계산하도록 한다.
     */
    @Tool(description =
        "두 숫자의 사칙연산을 수행합니다."
        + "operation은 add, subtract, multiply, divide 중 하나입니다.")
    public CalculatorResponse calculator (CalculatorRequest request) {

        log.info("[Calculator Tool] {} {} {}", request.getA(), request.getOperation(), request.getB());

        double result = switch (request.getOperation()) {

            case "add" -> request.getA() + request.getB();

            case "subtract" -> request.getA() - request.getB();

            case "multiply" -> request.getA() * request.getB();

            case "divide" -> {

                if (request.getB() == 0) {
                    throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
                }

                yield request.getA() / request.getB();
            }

            default -> throw new IllegalArgumentException("지원하지 않는 연산: " + request.getOperation());
        };

        return CalculatorResponse.builder()
            .result(result)
            .build();
    }


    /*
     *  3. 현재 시간 조회 Tool
     *
     *  LLM이 자체 지식으로 시간을 추측하지 않고 Java에서 현재 시간을 가져오도록 한다.
     */
    @Tool(description =
            "대한민국 서울 기준 현재 날짜와 시간을 조회합니다.")
    public CurrentTimeResponse getCurrentTime() {

        log.info("[CurrentTime Tool] 현재 시간 조회");

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        String isoFormat = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String readableFormat = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"));

        return CurrentTimeResponse.builder()
            .isoFormat(isoFormat)
            .readableFormat(readableFormat)
            .build();
    }
}
