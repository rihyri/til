package io.github.rihyri.til.week01.day04.example.service;

import io.github.rihyri.til.week01.day04.example.MenuRepository;
import io.github.rihyri.til.week01.day04.example.dto.MenuDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private MenuRepository menuRepository;

}
