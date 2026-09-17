package io.github.rihyri.til.week01.day03.example.repository;

import io.github.rihyri.til.week01.day03.example.entity.MemberReservation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    List<MemberReservation> findAllByMember_Id(Long memberId);
}
