package io.github.rihyri.til.week01.day03.example.service;

import io.github.rihyri.til.week01.day03.example.entity.Member;
import io.github.rihyri.til.week01.day03.example.repository.MemberRepository;
import io.github.rihyri.til.week01.day03.example.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("회원을 찾을 수 없습니다."));
    }

    @Transactional
    public Member createMember(String name, String email) {

        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 사용중인 이메일입니다.");
        }

        Member member = Member.builder()
                .name(name)
                .email(email)
                .build();

        return memberRepository.save(member);
    }

    @Transactional
    public void changeName(Long id, String name) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("회원을 찾을 수 없습니다."));

        member.changeName(name);
        // @Transactional 안에서 조회한 Entity는 영속 상태이기 때문에 값이 변경되면 JPA의 Dirty Checking(변경 감지)에 의해
        // 트랜잭션 종료 시점에 UPDATE가 수행될 수 있다. 따라서 memberRepository.save(member); 코드를 작성해주지 않아도 됐다.
        // 새 Entity 저장 → save() / 조회한 Entity 수정 → 변경 감지
    }

    @Transactional
    public void deleteMember(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("회원을 찾을 수 없습니다."));

        memberRepository.deleteById(id);
    }
}
