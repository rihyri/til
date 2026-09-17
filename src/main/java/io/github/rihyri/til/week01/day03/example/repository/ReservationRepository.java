package io.github.rihyri.til.week01.day03.example.repository;

import io.github.rihyri.til.week01.day03.example.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByMember_Id(long memberId);
}
