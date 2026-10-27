<div align="center">

# 📚 Today I Learned ①

### Bootcamp Learning Archive

**매일 배운 내용을 기록하고, 예시 코드를 다른 방식으로 작성하며 수업 내용을 이해했습니다.<br>
마지막은 하루의 회고를 통해 성장하는 학습 공간입니다.**

**부트캠프 수업과정의 1~5주차 과정을 담았습니다!**

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring_AI-6DB33F?style=flat-square&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-FF4438?style=flat-square&logo=redis&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)

</div>

---

## 01. About

부트캠프 수업에서 학습한 백엔드 개발 개념과 실습 코드를 정리하는 TIL(**Today I Learned**) 저장소입니다.

하루 동안 배운 내용을 나열하기보다는 **이해하고, 코드로 확인하고, 설명할 수 있도록** 기록합니다. 일일 학습 문서는 수업에서 배웠던 개념과 예제를 담고, 주간 정리 문서는 한 주를 돌아볼 수 있도록 했습니다.

## 02. Learning Goals

- **이해** — 새로운 개념과 동작 원리를 정리 합니다.
- **실습** — Java와 Spring을 활용해 학습한 내용을 다른 예제 코드로 변경하여 직접 구현합니다.
- **연결** — 개별 기술이 실제 백엔드 서비스에서 어떻게 이어지는지 생각합니다.
- **회고** — 매주 주요 학습 내용과 어려웠던 점을 돌아봅니다.

## 03. Tech Stack

| 구분 | 기술                                      |
| --- |-----------------------------------------|
| Language | Java                                    |
| Framework | Spring Boot, Spring AI, Spring Security |
| Database / Vector Search | PostgreSQL, pgvector, MySQL             |
| In-Memory Store | Redis                                   |
| Build | Gradle                                  |
| Documentation | Markdown, GitHub                        |


## 04. Learning Archive

부트캠프에서 학습한 내용을 주차별로 정리한 아카이브입니다.

매일 학습한 개념과 실습 내용을 기록하고, 주간 회고를 통해 핵심 내용을 복습합니다.

### 📚 Weekly Learning Roadmap

| Week | 핵심 학습 주제 | 학습 기록 | 주간 회고 |
| :---: | --- | :---: | :---: |
| **01** | Spring Framework, Spring Boot, JPA | [Week 01](src/main/java/io/github/rihyri/til/week01) | [Review](src/main/java/io/github/rihyri/til/week01/week1.md) |
| **02** | REST API, Validation, Transaction, QueryDSL | [Week 02](src/main/java/io/github/rihyri/til/week02) | [Review](src/main/java/io/github/rihyri/til/week02/week2.md) |
| **03** | Spring AI, LLM, Context, Multimodal | [Week 03](src/main/java/io/github/rihyri/til/week03) | [Review](src/main/java/io/github/rihyri/til/week03/week3.md) |
| **04** | Embedding, Vector DB, RAG, Advisor | [Week 04](src/main/java/io/github/rihyri/til/week04) | [Review](src/main/java/io/github/rihyri/til/week04/week04.md) |
| **05** | Session, Redis, Cache, Spring Security | [Week 05](src/main/java/io/github/rihyri/til/week05) | [Review](src/main/java/io/github/rihyri/til/week05/week5.md) |

---

### Week 01 · Spring Framework & JPA

Spring의 핵심 원리부터 Spring Boot 웹 애플리케이션 구조, JPA의 연관관계와 데이터 로딩 전략까지 학습합니다.

| Day | 학습 주제 | 문서 |
| :---: | --- | :---: |
| 01 | Spring Framework, IoC, DI, AOP, PSA | [학습 노트](src/main/java/io/github/rihyri/til/week01/day01/doc/day1.md) |
| 02 | Spring Boot, Layered Architecture, JPA, Flyway | [학습 노트](src/main/java/io/github/rihyri/til/week01/day02/doc/day2.md) |
| 03 | Spring Data JPA, 의존성 주입, 엔티티 연관관계 | [학습 노트](src/main/java/io/github/rihyri/til/week01/day03/doc/day3.md) |
| 04 | Self-Reference, DTO, JPA 데이터 로딩 전략 | [학습 노트](src/main/java/io/github/rihyri/til/week01/day04/doc/day4.md) |
| **Review** | Spring 핵심 원리와 JPA 학습 내용 종합 정리 | [주간 회고](src/main/java/io/github/rihyri/til/week01/week1.md) |

### Week 02 · REST API & JPA Advanced

Spring 기반 API 설계와 예외 처리, 데이터 검증, 트랜잭션 및 QueryDSL을 학습합니다.

| Day | 학습 주제 | 문서 |
| :---: | --- | :---: |
| 01 | Spring Controller, 공통 응답, 예외 처리 | [학습 노트](src/main/java/io/github/rihyri/til/week02/day01/doc/day1.md) |
| 02 | Validation, Service Layer, Builder, MapStruct | [학습 노트](src/main/java/io/github/rihyri/til/week02/day02/doc/day2.md) |
| 03 | Transaction, Persistence Context, QueryDSL | [학습 노트](src/main/java/io/github/rihyri/til/week02/day03/doc/day3.md) |
| **Review** | REST API 설계와 JPA 심화 개념 종합 정리 | [주간 회고](src/main/java/io/github/rihyri/til/week02/week2.md) |

### Week 03 · Spring AI & LLM

Spring AI를 활용하여 LLM과 연동하는 방법을 배우고, 대화 컨텍스트 관리와 멀티모달 AI를 학습합니다.

| Day | 학습 주제 | 문서 |
| :---: | --- | :---: |
| 01 | Spring AI 기초, AI와 LLM의 개념 | [학습 노트](src/main/java/io/github/rihyri/til/week03/day01/doc/day1.md) |
| 02 | Spring AI, LLM Message 구조 및 구현 | [학습 노트](src/main/java/io/github/rihyri/til/week03/day02/doc/day2.md) |
| 03 | LLM Context 관리, 대화 기록 저장 전략 | [학습 노트](src/main/java/io/github/rihyri/til/week03/day03/doc/day3.md) |
| 04 | Multimodal AI, Local LLM | [학습 노트](src/main/java/io/github/rihyri/til/week03/day04/doc/day04.md) |
| **Review** | Spring AI와 LLM 활용 및 컨텍스트 관리 정리 | [주간 회고](src/main/java/io/github/rihyri/til/week03/week3.md) |

### Week 04 · Embedding, Vector DB & RAG

텍스트 임베딩과 벡터 데이터베이스를 이해하고, RAG를 활용한 검색 기반 AI 응답 구조를 학습합니다.

| Day | 학습 주제 | 문서 |
| :---: | --- | :---: |
| 01 | Embedding, 벡터 유사도, Vector DB, pgvector | [학습 노트](src/main/java/io/github/rihyri/til/week04/day01/doc/day01.md) |
| 02 | RAG의 동작 구조와 유사도 검색 | [학습 노트](src/main/java/io/github/rihyri/til/week04/day02/doc/day02.md) |
| 03 | Spring AI Advisor, Chat Memory, Function Calling | [학습 노트](src/main/java/io/github/rihyri/til/week04/day03/doc/day03.md) |
| **Review** | Vector DB → RAG → Advisor → LLM의 전체 흐름 | [주간 회고](src/main/java/io/github/rihyri/til/week04/week04.md) |

### Week 05 · Session, Redis & Spring Security

HTTP 세션 관리부터 Redis 캐싱 전략과 Spring Security 인증 구조까지 학습합니다.

| Day | 학습 주제 | 문서 |
| :---: | --- | :---: |
| 01 | HTTP Session, Cookie, Session Clustering | [학습 노트](src/main/java/io/github/rihyri/til/week05/day01/doc/day01.md) |
| 02 | Redis, 자료구조, TTL, 영속성 | [학습 노트](src/main/java/io/github/rihyri/til/week05/day02/doc/day02.md) |
| 03 | Redis 캐싱 전략과 성능 관리 | [학습 노트](src/main/java/io/github/rihyri/til/week05/day03/doc/day03.md) |
| 04 | Spring Security와 Redis 기반 세션 인증 | [학습 노트](src/main/java/io/github/rihyri/til/week05/day04/doc/day04.md) |
| **Review** | Session → Redis → Spring Security의 전체 흐름 | [주간 회고](src/main/java/io/github/rihyri/til/week05/week5.md) |


<br>

