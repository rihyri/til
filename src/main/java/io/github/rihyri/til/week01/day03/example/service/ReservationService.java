package io.github.rihyri.til.week01.day03.example.service;

import io.github.rihyri.til.week01.day03.example.entity.MemberReservation;
import io.github.rihyri.til.week01.day03.example.entity.Reservation;
import io.github.rihyri.til.week01.day03.example.repository.MemberReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final MemberReservationRepository memberReservationRepository;

    public List<Reservation> getReservationsBMember(Long memberId) {

        return memberReservationRepository
                .findAllByMember_Id(memberId)
                .stream()
                .map(MemberReservation::getReservation)
                .toList();
    }
}
