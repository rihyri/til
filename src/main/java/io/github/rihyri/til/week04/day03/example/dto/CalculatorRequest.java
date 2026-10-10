package io.github.rihyri.til.week04.day03.example.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class CalculatorRequest {

    double a;

    double b;

    String operation;
}
