# Exerciting

[![CI](https://github.com/ytw1214/Exerciting/actions/workflows/ci.yml/badge.svg)](https://github.com/ytw1214/Exerciting/actions/workflows/ci.yml)

스포츠 직관 동행 매칭 서비스입니다. 경기 일정·팀 순위 크롤링, 직관 매칭, 실시간 채팅, JWT 인증을 제공합니다.

## 실행 방법

필요: JDK 17, Docker

```bash
cp .env.example .env          # JWT_SECRET, DB_PASSWORD, DB_ROOT_PASSWORD 채우기
docker compose up -d          # MySQL 8.0
./gradlew bootRun             # http://localhost:8080/swagger-ui/index.html
```

## 테스트와 검증

| 명령 | 내용 |
|---|---|
| `./gradlew test` | 단위·통합 테스트 (H2, PR과 main 푸시마다 CI에서 실행) |
| `./gradlew perfTest` | 채팅 10만 건 삽입과 최근 50건 조회 측정 (로컬 MySQL 필요) |
| `k6 run -e GAME_ID=1 k6/smoke.js` | 실행 중인 서버의 핵심 흐름 스모크 테스트 |
| `k6 run -e GAME_ID=1 k6/join-concurrency.js` | 실제 MySQL에서 동시 참가 정원 보장 확인 |

## 기술 스택

| 구분 | 스택 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5, Spring Security, Spring WebSocket (STOMP) |
| DB / ORM | MySQL 8.0, Spring Data JPA, QueryDSL 5.0 |
| 인증 | JWT (jjwt) |
| 크롤링 | Jsoup, Selenium |
| 테스트 / CI | JUnit 5, H2, GitHub Actions |
| 로컬 환경 | Docker Compose (MySQL) |
| 문서화 | Springdoc OpenAPI (Swagger) |

## 주요 구현 및 기술적 경험

### 1. MongoDB로 옮기기 전에, 구간별 측정으로 채팅 조회 병목 해결

**상황**
채팅 저장소를 MongoDB로 옮길지 판단하기 위해 100개 방 × 방당 1,000건, 총 10만 건의 메시지로 두 DB를 비교했습니다. 특정 방의 최근 50건 조회가 MySQL은 평균 약 323ms, MongoDB는 약 10ms로 30배 넘게 차이 났습니다. 처음에는 설정을 바꿀 때마다 결과가 흔들려, 조건을 고정하고 워밍업 5회 뒤 20회 평균을 3번씩 잰 값만 기준으로 삼았습니다.

**원인**
DB를 바로 바꾸지 않고 시간이 어디서 쓰이는지 나눠 측정했습니다.

| 확인 항목 | 결과 |
|---|---|
| 같은 SQL을 JDBC로 직접 실행 | 약 5ms → DB 자체는 원인이 아님 |
| 트랜잭션 비용 | 약 3~4ms → 원인이 아님 |
| 실제 실행된 쿼리 (performance_schema) | JPA가 만든 JOIN 쿼리만 느림 |
| 측정 시점의 실행 계획 | 예상 행 수 0.05, 보낸 사람 인덱스로 10만 건 조회 |

대량 삽입 직후 통계가 갱신되지 않아, 옵티마이저가 채팅방이 아닌 보낸 사람 인덱스를 고르고 있었습니다.

**해결**
`ANALYZE TABLE`로 통계를 갱신해 원인을 확인한 뒤, 조회 조건과 정렬에 맞는 복합 인덱스를 추가했습니다.

```java
@Table(indexes = @Index(
        name = "idx_chat_room_send_at",
        columnList = "chatroom_id, send_at"))
public class MatchingChat {
```

**결과** (2026.09 로컬 PC, 각 3회 평균)

| 단계 | JPA 조회 | JDBC 조회 | 읽은 행 |
|---|---|---|---|
| 개선 전 | 약 323ms | 약 5.4ms | 10만 건 |
| 통계 갱신 | 약 13.1ms | 약 5.5ms | 1,000건 + 정렬 |
| 복합 인덱스 추가 | 약 14.5ms | 약 3.0ms | 51건, 정렬 없음 |

조회가 약 13ms로 MongoDB(약 10ms)와 큰 차이가 없어졌고, 원인이 DB가 아니었기 때문에 운영할 DB를 늘리지 않고 MySQL을 유지했습니다.

**다른 환경에서 재측정** (2026.10, Docker MySQL 8.0 / MongoDB 7.0, 3회 평균)

| 항목 | MySQL | MongoDB |
|---|---|---|
| 최근 50건 조회 (프레임워크 포함) | 약 4.07ms (JPA) | 약 2.69ms |
| 같은 쿼리, DB만 | 약 1.09ms (JDBC) | — |
| 실제로 읽은 행 | 1,000건 + 정렬 (JPA 쿼리) | 51건 |

환경을 바꿔 다시 재도 조회 차이는 1~2ms였고, DB만 보면 MySQL이 더 빨랐습니다.

**두 DB가 같은 일을 하는지 확인**
MongoDB 도큐먼트에는 보낸 사람 id만 있었지만, MySQL의 JPA 쿼리는 users 테이블을 JOIN해 보낸 사람 정보까지 가져오고 있었습니다. 실행 계획을 보니 이 JOIN 때문에 해시 조인이 선택되어 방 메시지 1,000건을 모두 읽고 정렬했고, JOIN 없이 조회하면 같은 인덱스로 51건만 읽었습니다. 지금은 차이가 작지만, 방에 메시지가 쌓일수록 읽는 양도 함께 늘어나는 구조입니다.

도큐먼트처럼 닉네임을 메시지에 함께 저장하면 JOIN은 사라지지만, 닉네임이 바뀔 때 지난 메시지를 모두 고쳐야 합니다. 그래서 MySQL에서는 테이블을 나눈 구조를 유지하고, 보낸 사람 정보를 따로 일괄 조회해 방 크기와 관계없이 51건만 읽도록 개선할 예정입니다.

**한계**
테스트 데이터의 보낸 사람이 1명뿐이라, 9월의 잘못된 인덱스 선택과 10월의 해시 조인 모두 실제 서비스보다 크게 드러났을 수 있습니다. 보낸 사람을 여러 명으로 나눈 데이터로 다시 측정할 예정입니다.

관련 PR: [#12 채팅 조회 병목 원인 확인 및 복합 인덱스 적용](https://github.com/ytw1214/Exerciting/pull/12) · 재측정 코드: [`experiment/mongo-compare`](https://github.com/ytw1214/Exerciting/tree/experiment/mongo-compare) 브랜치

---

### 2. 10만 건 삽입 20.5초 → 4.87초: 틀린 가설을 측정으로 바로잡은 대량 저장

**상황**
채팅 메시지가 방마다 계속 쌓이는 구조라, 10만 건을 넣어 이 규모에서도 저장이 버티는지 확인했습니다. 삽입에 20.5초가 걸렸습니다.

**첫 번째 가설**
PK 전략이 `IDENTITY`라 Hibernate가 INSERT 직후 생성된 PK를 바로 확인해야 해서 묶어서 저장할 수 없다고 보고, `SEQUENCE(allocationSize=1000)`로 바꾸고 Hibernate 배치 크기를 1000으로 설정했습니다. 그런데 다시 재보니 17.7초로 거의 줄지 않았습니다.

**실제 원인**
Hibernate 설정이 아니라 드라이버 쪽을 의심해 찾아보니, MySQL 드라이버는 기본 설정에서 배치를 준비해도 한 건씩 전송하고 있었습니다. `rewriteBatchedStatements=true` 옵션을 추가했습니다.

```properties
# JDBC 드라이버 레벨 - INSERT를 실제로 하나로 묶어서 전송
spring.datasource.url=jdbc:mysql://...?rewriteBatchedStatements=true
```
```properties
# Hibernate 레벨 - SEQUENCE allocationSize와 짝을 맞춤
spring.jpa.properties.hibernate.jdbc.batch_size=1000
spring.jpa.properties.hibernate.order_inserts=true
```

**결과** (2026.09 로컬 PC)

| 단계 | 10만 건 삽입 |
|---|---|
| IDENTITY | 20.5초 |
| SEQUENCE 전환 + Hibernate 배치 | 17.7초 |
| + `rewriteBatchedStatements=true` | 4.87초 (약 76% 단축) |

이후로는 설정만 믿지 않도록, 측정할 때마다 MySQL이 실제로 실행한 INSERT 문 수를 세어 10만 건이 1,000건씩 100회로 전송되는지 확인합니다(`./gradlew perfTest`). Docker 환경 재측정(2026.10, 3회 평균)에서는 MySQL 약 6.9초, MongoDB 약 1.4초로 삽입은 MongoDB가 빨랐지만, 실제 채팅은 메시지를 한 건씩 저장하므로 저장소 판단은 조회 성능을 기준으로 했습니다.

**추가 개선**
삽입 성능만 신경 쓰고 조회는 손대지 않았다는 걸 뒤늦게 확인해, 채팅방 입장 시 전체 메시지를 한 번에 긁어오던 방식을 최근 50건만 가져오는 페이지네이션(`Slice`)으로 변경했습니다. `Page` 대신 `Slice`를 쓴 이유는 count 쿼리 없이 "다음 데이터가 더 있는지"만 확인하면 되는 무한 스크롤 방식이 채팅 UX에 더 맞기 때문입니다.

---

### 3. 비관적 락으로 매칭 정원 보장

**상황**
매칭 참가 기능에서 여러 사용자가 동시에 마지막 자리를 요청하면 정원이 초과될 수 있는 구조였습니다.

**해결**
낙관적 락은 실패 시 재시도 로직이 따로 필요해서, 요청 시점에 바로 정합성을 보장하는 게 맞다고 판단해 비관적 락(Pessimistic Lock)을 적용했습니다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT m FROM Matching m WHERE m.id = :id")
Optional<Matching> findByIdWithLock(@Param("id") Long id);
```

**결과**
정원 5명 매칭에 100명이 동시에 참가를 요청하는 통합 테스트(`MatchingJoinConcurrencyTest`)로, 호스트 포함 정확히 5명만 들어가고 나머지 96건은 정원 초과로 거절되는 것을 검증합니다. 이 테스트는 PR마다 CI에서 실행됩니다. 다만 H2(MySQL 모드)에서 도는 회귀 테스트라, 실제 MySQL에서의 확인은 `k6/join-concurrency.js`로 따로 할 예정입니다.

**추가 개선**
참가(`joinMatching`)에만 락을 걸고 퇴장(`leaveMatching`)에는 락이 없어, 참가와 퇴장이 동시에 발생하면 참여 인원 계산이 어긋날 수 있는 지점을 코드 리뷰 중 발견해 동일한 락을 적용했습니다. "정원을 늘리는 경로"만 락을 걸고 "줄이는 경로"는 놓치기 쉬운 부분이라, 앞으로 동시성 처리 시 진입/이탈 양쪽을 함께 점검하는 체크리스트로 삼고 있습니다.

---

### 4. QueryDSL 기반 동적 검색 구현 및 N+1 개선

**상황**
종목, 경기 날짜, 팀명을 조합해서 검색해야 했는데, JPQL로 조건을 하나씩 관리하다 보니 조합이 늘어날 때마다 메서드가 같이 늘어나는 문제가 있었습니다. 또한 목록 조회 시 팀·경기장 연관 엔티티의 LAZY 로딩 때문에 N+1이 발생하는 걸 발견했습니다.

**해결**
QueryDSL로 바꿔서 동적 쿼리를 타입 세이프하게 구성했고, `Projections.constructor` + `leftJoin`으로 연관 엔티티에서 필요한 컬럼만 한 번에 가져오도록 고쳤습니다.

**결과**
검색 쿼리 실행 횟수가 1회로 줄어드는 걸 확인했습니다. 매칭 목록 조회에도 같은 문제가 있어 `@EntityGraph`로 경기·팀·경기장을 함께 가져오도록 했고, 매칭 10건 목록이 목록 1회 + count 1회, 총 2회의 쿼리로 끝나는 것을 테스트(`MatchingListQueryCountTest`)로 검증합니다.

---

### 5. 예외 처리 계층 복구와 로그 레벨 분리

**상황**
API 응답 형식을 통일하려고 `ErrorCode` + `BusinessException` + `GlobalExceptionHandler` 구조를 도입했는데, 정작 그 이전부터 있던 커스텀 예외들을 새 부모로 옮기는 작업을 하지 않은 채로 남아 있었습니다. 결과적으로 `@ExceptionHandler(BusinessException.class)`는 잡을 자식이 하나도 없었고, 모든 예외가 최종 `Exception` 핸들러로 떨어져 **`ErrorCode`에 404·401·409로 정의해둔 응답이 전부 500으로 나가고 있었습니다.**

```java
// 핸들러는 BusinessException을 기다리는데
@ExceptionHandler(BusinessException.class)

// 실제 예외는 RuntimeException을 직접 상속 → 잡히지 않음
public class MatchingNotFoundException extends RuntimeException { ... }
```

**해결**
예외를 **클라이언트가 요청을 고쳐서 해결할 수 있는 실패(4xx)** 와 **서버 측에서 조치해야 하는 장애(5xx)** 로 나누고, 전자만 `BusinessException` 아래로 모았습니다.

| 구분 | 예시 | 처리 |
|---|---|---|
| 클라이언트 교정 가능 (4xx) | `MatchingNotFound`, `UnauthorizedUser`, `DuplicateResource` | `BusinessException` 상속 → `ErrorCode`의 상태코드로 응답, `WARN` 단문 로그 |
| 서버·외부 의존 장애 (5xx) | `CrawlingException` | 별도 핸들러 → `502`, `ERROR` + 스택트레이스 |

`CrawlingException`을 제외한 이유는, 크롤링 실패의 원인이 대부분 KBO 사이트의 HTML 구조 변경이라 **어느 셀렉터에서 끊겼는지 스택트레이스가 남아야** 하기 때문입니다. `BusinessException`으로 묶으면 `WARN` 한 줄만 남아 디버깅이 불가능해집니다. 상태코드도 서버 자체 오류가 아니라 의존하는 외부 소스의 문제이므로 500 대신 502를 사용했습니다.

또한 `BusinessException`을 `abstract`로 바꿔 직접 생성을 차단하고, 자식마다 중복 선언돼 있던 `errorCode` 필드를 부모로 올렸습니다. 자식 클래스가 8줄에서 3줄로 줄었고, 부모 필드를 가리는(shadowing) 위험도 함께 제거했습니다.

**결과**

```
# Before
GET /api/v1/matching/99999
→ 500  {"code":"INTERNAL_SERVER_ERROR","message":"서버 오류가 발생했습니다."}

# After
GET /api/v1/matching/99999
→ 404  {"code":"MATCHING_NOT_FOUND","message":"해당 매칭을 찾을 수 없습니다."}
```

부수적으로 로그도 정리됐습니다. 이전에는 존재하지 않는 리소스를 조회할 때마다 스택트레이스가 `ERROR`로 쌓여서, 정작 확인해야 할 진짜 장애가 묻히는 상태였습니다. 지금은 예상된 실패는 `WARN` 한 줄, 조치가 필요한 장애만 `ERROR` + 스택트레이스로 남습니다.

**추가 개선**
같은 점검 과정에서 `TeamService`가 `IllegalArgumentException`을 던지고 있는 것도 발견해 `TeamNotFoundException`으로 교체했습니다. 표준 예외를 쓰면 편하지만 `BusinessException` 계층 밖이라 똑같이 500으로 나가고, 무엇보다 "팀을 못 찾았다"는 의미가 상태코드에 드러나지 않습니다. 공통 예외 구조를 만들었다면 **표준 예외를 던지는 지점이 남아 있지 않은지 함께 확인해야 한다**는 걸 이번에 정리했습니다.

이후 코드 리뷰에서 잘못된 JSON, 파라미터 타입 불일치 같은 Spring MVC 표준 예외도 `Exception` 핸들러에 먼저 잡혀 500으로 나가던 것을 발견해, `GlobalExceptionHandler`가 `ResponseEntityExceptionHandler`를 상속하도록 바꿔 원래의 4xx로 응답하게 했습니다.

---

### 6. Selenium 크롤링과 WebSocket 실시간 채팅

**상황**
KBO 사이트가 JavaScript(PostBack) 기반이라 Jsoup만으로는 경기 일정 데이터를 가져올 수 없었고, 실시간 채팅에는 로그인하지 않은 사용자의 연결을 막을 방법이 필요했습니다.

**해결**
Selenium으로 크롤링을 구현했고, 채팅은 STOMP 기반으로 붙이면서 WebSocket CONNECT 단계에서 JWT를 검증하는 인터셉터를 적용했습니다. 이후 코드 리뷰에서 구독(SUBSCRIBE) 단계 인가가 빠져 로그인한 누구나 다른 채팅방을 구독할 수 있던 것을 발견해, 매칭 참가자만 구독하도록 고쳤습니다.

```java
if (StompCommand.CONNECT.equals(accessor.getCommand())) {
    authenticate(accessor);          // JWT 검증 후 세션에 사용자 등록
} else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
    authorizeSubscribe(accessor);    // 해당 채팅방 참가자인지 확인
}
```
```java
if (!matchingChatRoomService.isJoinedMember(chatRoomId, loginUser.getId())) {
    throw new MessagingException("채팅방 참가자만 구독할 수 있습니다.");
}
```

**결과**
Jsoup으로는 불가능했던 동적 페이지 크롤링이 가능해졌고, 인증되지 않은 사용자의 연결과 참가자가 아닌 사용자의 채팅방 구독은 차단되도록 했습니다.

---

### 7. 코드 리뷰로 찾은 결함과 재발 방지

**상황**
기능을 다 만든 뒤, 제가 놓친 결함을 찾기 위해 AI에게 코드 전체를 비판적으로 리뷰하게 했습니다.

**해결**
받은 수정안은 바로 반영하지 않고, 커밋 단위로 읽고 테스트로 검증한 뒤 반영했습니다.

| 발견한 결함 | 영향 | 조치 |
|---|---|---|
| 호스트 참가 행 중복 저장 | 매칭 생성이 유니크 제약에 걸려 실패. 한 번 고친 버그가 병합 충돌을 해결하다 되살아남 | 수정 후 PR마다 테스트를 돌리는 CI 도입 |
| 공개 저장소 이력의 DB 비밀번호와 JWT 키 | 이력은 gitignore로도 지워지지 않음 | 키를 교체하고 환경 변수로 분리 |
| AI 수정안이 기존 테스트를 깨뜨림 | 테스트 5개 실패 (자동설정 패키지를 찾지 못함) | 원인(중첩 `@Configuration`)을 찾아 `@TestConfiguration`으로 직접 수정 |

**결과**
수정 사항을 [PR #16](https://github.com/ytw1214/Exerciting/pull/16)으로 main에 반영했고(커밋 19개), 이후 모든 PR과 main 푸시에서 테스트가 자동으로 실행됩니다.

## 다음 계획

- [x] CI(GitHub Actions)로 PR 시 테스트 자동 실행
- [x] `joinMatching` 동시 참가 시나리오에 대한 멀티스레드 테스트 추가
- [ ] 락을 제거했을 때 정원이 얼마나 초과되는지 측정해 기재
- [ ] N+1 개선 전 쿼리 수 측정 및 기재
- [ ] k6로 실제 MySQL에서 동시 참가와 핵심 흐름 측정
- [ ] 채팅 조회: `join fetch` 대신 보낸 사람 정보를 따로 일괄 조회해 방 크기와 관계없이 51건만 읽도록 개선
- [ ] 보낸 사람을 여러 명으로 나눈 데이터로 채팅 조회 재측정
- [ ] 모든 커스텀 예외가 `BusinessException`을 상속하는지 검증하는 회귀 테스트 추가
- [ ] 채팅방 목록 + 안 읽은 메시지 수(unread count) API 구현
