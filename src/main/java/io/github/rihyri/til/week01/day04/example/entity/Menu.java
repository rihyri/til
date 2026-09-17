package io.github.rihyri.til.week01.day04.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 자기 자신(Menu)을 부모로 가진다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    // 현재 Menu를 부모로 가진 하위 메뉴들
    @OneToMany(mappedBy = "parent")
    private List<Menu> children = new ArrayList<>();
}

/**
 *  id | name    | parent_id
 *  ------------------------
 *  1  | 고객센터  | null
 *  2  | 공지사항  | 1
 *  3  | FAQ     | 1
 *  4  | 문의하기  | 1
 *
 */