# Exerciting

스포츠 팀 랭킹 크롤링 + 실시간 매칭/채팅 서비스

> 개발 과정을 시간순으로 기록한 일지는 [docs/DEVLOG.md](docs/DEVLOG.md)에 별도로 남겨두었습니다.

## 기술 스택

| 구분 | 스택 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5, Spring Security, Spring WebSocket (STOMP) |
| DB / ORM | MySQL 8.0, Spring Data JPA, QueryDSL 5.0 |
| 인증 | JWT (jjwt) |
| 크롤링 | Jsoup, Selenium |
| 문서화 | Springdoc OpenAPI (Swagger) |
| 인프라 | Docker, Docker Compose |
## 주요 구현 및 기술적 경험

### 1. 동시성 제어를 통한 매칭 정원 보장

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
동시에 여러 참가 요청이 들어와도 정원을 초과하지 않는 것을 확인했습니다.

**추가 개선**
참가(`joinMatching`)에만 락을 걸고 퇴장(`leaveMatching`)에는 락이 없어, 참가와 퇴장이 동시에 발생하면 참여 인원 계산이 어긋날 수 있는 지점을 코드 리뷰 중 발견해 동일한 락을 적용했습니다. "정원을 늘리는 경로"만 락을 걸고 "줄이는 경로"는 놓치기 쉬운 부분이라, 앞으로 동시성 처리 시 진입/이탈 양쪽을 함께 점검하는 체크리스트로 삼고 있습니다.

---

### 2. QueryDSL 기반 동적 검색 구현 및 N+1 개선

**상황**
종목, 경기 날짜, 팀명을 조합해서 검색해야 했는데, JPQL로 조건을 하나씩 관리하다 보니 조합이 늘어날 때마다 메서드가 같이 늘어나는 문제가 있었습니다. 또한 목록 조회 시 팀·경기장 연관 엔티티의 LAZY 로딩 때문에 N+1이 발생하는 걸 발견했습니다.

**해결**
QueryDSL로 바꿔서 동적 쿼리를 타입 세이프하게 구성했고, `Projections.constructor` + `leftJoin`으로 연관 엔티티를 fetch join 방식으로 한 번에 가져오도록 고쳤습니다.

**결과**
쿼리 실행 횟수가 1회로 줄어드는 걸 확인했습니다.

> 참고: 개선 전 정확한 쿼리 발생 횟수(N값)는 별도로 측정해 기재할 예정입니다.

---

### 3. 채팅 메시지 대량 삽입 성능 개선 (SEQUENCE 전략 도입)

**상황**
채팅 메시지가 방별로 계속 쌓이는 구조로, 100개 방 × 1,000건씩 총 10만 건을 넣어보고 실제로 저장 방식이 이 규모에서도 괜찮은지 확인해봤습니다. 처음엔 삽입에 20.5초가 걸렸습니다.

**해결**
PK 전략이 `IDENTITY`라 Hibernate가 INSERT 실행 직후 생성된 PK를 바로 확인해야 해서 배치 삽입 자체가 안 된다는 걸 알고 `SEQUENCE(allocationSize=1000)`으로 바꿔봤습니다. 근데 다시 재보니 20.5초에서 17.7초로 별 차이가 없었고, Hibernate 설정 문제가 아니라 JDBC 드라이버 쪽을 의심했습니다. 찾아보니 MySQL 드라이버가 기본 설정으로는 실제로는 개별 전송한다는 걸 알게 돼 `rewriteBatchedStatements=true` 옵션을 추가했습니다.

```properties
# JDBC 드라이버 레벨 - INSERT를 실제로 하나로 묶어서 전송
spring.datasource.url=jdbc:mysql://...?rewriteBatchedStatements=true
```
```properties
# Hibernate 레벨 - SEQUENCE allocationSize와 짝을 맞춤
spring.jpa.properties.hibernate.jdbc.batch_size=1000
spring.jpa.properties.hibernate.order_inserts=true
```

**결과**
삽입 시간이 20.5초에서 4.87초로 줄었습니다(약 76% 단축). 같은 조건으로 MongoDB에도 똑같이 넣어보니, 튜닝 전에는 MongoDB가 15배 가까이 빨랐는데 튜닝 후에는 격차가 3.7배 정도로 줄었습니다. 조회 속도도 79ms와 83ms로 거의 차이가 없었고요. 그래서 지금 이 정도 데이터 규모에서는 MySQL을 옮기지 않고 설정 개선 쪽으로 풀었습니다. (MongoDB 비교 실험 코드는 `spike/mongo-comparison` 브랜치에 별도로 남겨두었습니다.)

**추가 개선**
삽입 성능만 신경 쓰고 조회는 손대지 않았다는 걸 뒤늦게 확인해, 채팅방 입장 시 전체 메시지를 한 번에 긁어오던 방식을 최근 50건만 가져오는 페이지네이션(`Slice`)으로 변경했습니다. `Page` 대신 `Slice`를 쓴 이유는 count 쿼리 없이 "다음 데이터가 더 있는지"만 확인하면 되는 무한 스크롤 방식이 채팅 UX에 더 맞기 때문입니다.

---

### 4. Selenium 크롤링과 WebSocket 실시간 채팅

**상황**
KBO 사이트가 JavaScript(PostBack) 기반이라 Jsoup만으로는 경기 일정 데이터를 가져올 수 없었고, 실시간 채팅에는 로그인하지 않은 사용자의 연결을 막을 방법이 필요했습니다.

**해결**
Selenium으로 크롤링을 구현했고, 채팅은 STOMP 기반으로 붙이면서 WebSocket CONNECT 단계에서 JWT를 검증하는 인터셉터를 적용했습니다.

```java
if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
    String token = accessor.getFirstNativeHeader("Authorization");
    if (token == null || !jwtTokenProvider.validateToken(token)) {
        throw new MessagingException("인증에 실패했습니다.");
    }
}
```

**결과**
Jsoup으로는 불가능했던 동적 페이지 크롤링이 가능해졌고, 인증되지 않은 사용자의 WebSocket 연결이 차단되는 것을 확인했습니다.

## 다음 계획

- [ ] N+1 개선 전/후 정확한 쿼리 횟수 측정 및 기재
- [ ] `joinMatching` 동시 참가 시나리오에 대한 멀티스레드 테스트 추가
- [ ] 채팅방 목록 + 안 읽은 메시지 수(unread count) API 구현
- [ ] CI(GitHub Actions)로 PR 시 테스트 자동 실행
