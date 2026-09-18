package io.github.rihyri.til.week01.day04.example.service;

import io.github.rihyri.til.week01.day04.example.MenuRepository;
import io.github.rihyri.til.week01.day04.example.dto.MenuDto;
import io.github.rihyri.til.week01.day04.example.entity.Menu;
import lombok.RequiredArgsConstructor;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private MenuRepository menuRepository;

    public MenuDto.Parent getMenu(Long menuId) {

        // 1. 부모 메뉴 조회
        Menu parentMenu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다."));

        // 2. 해당 메뉴를 부모로 가지고 있는 하위 메뉴 조회
        List<Menu> childMenus = menuRepository.findByParent_Id(parentMenu.getId());

        // 3. Menu Entity 목록 → Child DTO 목록으로 변환
        List<MenuDto.Child> children = childMenus.stream()
                .map(menu -> MenuDto.Child.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .build())
                .toList();

        // 4. 부모 + 자식 목록을 하나의 DTO로 생성
        return MenuDto.Parent.builder()
                .id(parentMenu.getId())
                .name(parentMenu.getName())
                .children(children)
                .build();
    }
}
