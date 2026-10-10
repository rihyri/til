package io.github.rihyri.til.week04.day03.example.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrentTimeResponse {

    String isoFormat;

    String readableFormat;
}
