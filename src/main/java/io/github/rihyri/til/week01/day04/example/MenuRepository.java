package io.github.rihyri.til.week01.day04.example;

import io.github.rihyri.til.week01.day04.example.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // parent_id가 특정 id인 Menu 조회
    List<Menu> findByParent_Id(Long parentId);
}
