package io.github.rihyri.til.week04.day03.example.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherResponse {

    String city;

    Integer temperature;

    String condition;

    LocalDateTime timestamp;
}
