Exerciting

스포츠 팀 랭킹 크롤링 + 실시간 매칭/채팅 서비스

기술 스택
구분	스택
Language	Java 17
Framework	Spring Boot 3.5, Spring Security, Spring WebSocket (STOMP)
DB / ORM	MySQL 8.0, Spring Data JPA, QueryDSL 5.0
인증	JWT (jjwt)
크롤링	Jsoup, Selenium
문서화	Springdoc OpenAPI (Swagger)
주요 구현 및 기술적 경험
1. 동시성 제어를 통한 매칭 정원 보장

상황 매칭 참가 기능에서 여러 사용자가 동시에 마지막 자리를 요청하면 정원이 초과될 수 있는 구조였습니다.

해결 낙관적 락은 실패 시 재시도 로직이 따로 필요해서, 요청 시점에 바로 정합성을 보장하는 게 맞다고 판단해 비관적 락(Pessimistic Lock)을 적용했습니다.

java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT m FROM Matching m WHERE m.id = :id")
Optional<Matching> findByIdWithLock(@Param("id") Long id);

결과 동시에 여러 참가 요청이 들어와도 정원을 초과하지 않는 것을 확인했습니다.

추가 개선 참가(joinMatching)에만 락을 걸고 퇴장(leaveMatching)에는 락이 없어, 참가와 퇴장이 동시에 발생하면 참여 인원 계산이 어긋날 수 있는 지점을 코드 리뷰 중 발견해 동일한 락을 적용했습니다. "정원을 늘리는 경로"만 락을 걸고 "줄이는 경로"는 놓치기 쉬운 부분이라, 앞으로 동시성 처리 시 진입/이탈 양쪽을 함께 점검하는 체크리스트로 삼고 있습니다.

2. QueryDSL 기반 동적 검색 구현 및 N+1 개선

상황 종목, 경기 날짜, 팀명을 조합해서 검색해야 했는데, JPQL로 조건을 하나씩 관리하다 보니 조합이 늘어날 때마다 메서드가 같이 늘어나는 문제가 있었습니다. 또한 목록 조회 시 팀·경기장 연관 엔티티의 LAZY 로딩 때문에 N+1이 발생하는 걸 발견했습니다.

해결 QueryDSL로 바꿔서 동적 쿼리를 타입 세이프하게 구성했고, Projections.constructor + leftJoin으로 연관 엔티티를 fetch join 방식으로 한 번에 가져오도록 고쳤습니다.

결과 쿼리 실행 횟수가 1회로 줄어드는 걸 확인했습니다.

참고: 개선 전 정확한 쿼리 발생 횟수(N값)는 별도로 측정해 기재할 예정입니다.

3. 채팅 메시지 대량 삽입 성능 개선 (SEQUENCE 전략 도입)

상황 채팅 메시지가 방별로 계속 쌓이는 구조로, 100개 방 × 1,000건씩 총 10만 건을 넣어보고 실제로 저장 방식이 이 규모에서도 괜찮은지 확인해봤습니다. 처음엔 삽입에 20.5초가 걸렸습니다.

해결 PK 전략이 IDENTITY라 Hibernate가 INSERT 실행 직후 생성된 PK를 바로 확인해야 해서 배치 삽입 자체가 안 된다는 걸 알고 SEQUENCE(allocationSize=1000)으로 바꿔봤습니다. 근데 다시 재보니 20.5초에서 17.7초로 별 차이가 없었고, Hibernate 설정 문제가 아니라 JDBC 드라이버 쪽을 의심했습니다. 찾아보니 MySQL 드라이버가 기본 설정으로는 실제로는 개별 전송한다는 걸 알게 돼 rewriteBatchedStatements=true 옵션을 추가했습니다.

properties
# JDBC 드라이버 레벨 - INSERT를 실제로 하나로 묶어서 전송
spring.datasource.url=jdbc:mysql://...?rewriteBatchedStatements=true
properties
# Hibernate 레벨 - SEQUENCE allocationSize와 짝을 맞춤
spring.jpa.properties.hibernate.jdbc.batch_size=1000
spring.jpa.properties.hibernate.order_inserts=true

결과 삽입 시간이 20.5초에서 4.87초로 줄었습니다(약 76% 단축). 같은 조건으로 MongoDB에도 똑같이 넣어보니, 튜닝 전에는 MongoDB가 15배 가까이 빨랐는데 튜닝 후에는 격차가 3.7배 정도로 줄었습니다. 조회 속도도 79ms와 83ms로 거의 차이가 없었고요. 그래서 지금 이 정도 데이터 규모에서는 MySQL을 옮기지 않고 설정 개선 쪽으로 풀었습니다. (MongoDB 비교 실험 코드는 spike/mongodb-comparision 브랜치에 별도로 남겨두었습니다.)

추가 개선 삽입 성능만 신경 쓰고 조회는 손대지 않았다는 걸 뒤늦게 확인해, 채팅방 입장 시 전체 메시지를 한 번에 긁어오던 방식을 최근 50건만 가져오는 페이지네이션(Slice)으로 변경했습니다. Page 대신 Slice를 쓴 이유는 count 쿼리 없이 "다음 데이터가 더 있는지"만 확인하면 되는 무한 스크롤 방식이 채팅 UX에 더 맞기 때문입니다.

4. 예외 처리 계층 복구와 로그 레벨 분리

상황 API 응답 형식을 통일하려고 ErrorCode + BusinessException + GlobalExceptionHandler 구조를 도입했는데, 정작 그 이전부터 있던 커스텀 예외들을 새 부모로 옮기는 작업을 하지 않은 채로 남아 있었습니다. 결과적으로 @ExceptionHandler(BusinessException.class)는 잡을 자식이 하나도 없었고, 모든 예외가 최종 Exception 핸들러로 떨어져 ErrorCode에 404·401·409로 정의해둔 응답이 전부 500으로 나가고 있었습니다.

java
// 핸들러는 BusinessException을 기다리는데
@ExceptionHandler(BusinessException.class)

// 실제 예외는 RuntimeException을 직접 상속 → 잡히지 않음
public class MatchingNotFoundException extends RuntimeException { ... }

해결 예외를 클라이언트가 요청을 고쳐서 해결할 수 있는 실패(4xx) 와 서버 측에서 조치해야 하는 장애(5xx) 로 나누고, 전자만 BusinessException 아래로 모았습니다.

구분	예시	처리
클라이언트 교정 가능 (4xx)	MatchingNotFound, UnauthorizedUser, DuplicateResource	BusinessException 상속 → ErrorCode의 상태코드로 응답, WARN 단문 로그
서버·외부 의존 장애 (5xx)	CrawlingException	별도 핸들러 → 502, ERROR + 스택트레이스

CrawlingException을 제외한 이유는, 크롤링 실패의 원인이 대부분 KBO 사이트의 HTML 구조 변경이라 어느 셀렉터에서 끊겼는지 스택트레이스가 남아야 하기 때문입니다. BusinessException으로 묶으면 WARN 한 줄만 남아 디버깅이 불가능해집니다. 상태코드도 서버 자체 오류가 아니라 의존하는 외부 소스의 문제이므로 500 대신 502를 사용했습니다.

또한 BusinessException을 abstract로 바꿔 직접 생성을 차단하고, 자식마다 중복 선언돼 있던 errorCode 필드를 부모로 올렸습니다. 자식 클래스가 8줄에서 3줄로 줄었고, 부모 필드를 가리는(shadowing) 위험도 함께 제거했습니다.

결과

# Before
GET /api/v1/matching/99999
→ 500  {"code":"INTERNAL_SERVER_ERROR","message":"서버 오류가 발생했습니다."}

# After
GET /api/v1/matching/99999
→ 404  {"code":"MATCHING_NOT_FOUND","message":"해당 매칭을 찾을 수 없습니다."}

부수적으로 로그도 정리됐습니다. 이전에는 존재하지 않는 리소스를 조회할 때마다 스택트레이스가 ERROR로 쌓여서, 정작 확인해야 할 진짜 장애가 묻히는 상태였습니다. 지금은 예상된 실패는 WARN 한 줄, 조치가 필요한 장애만 ERROR + 스택트레이스로 남습니다.

추가 개선 같은 점검 과정에서 TeamService가 IllegalArgumentException을 던지고 있는 것도 발견해 TeamNotFoundException으로 교체했습니다. 표준 예외를 쓰면 편하지만 BusinessException 계층 밖이라 똑같이 500으로 나가고, 무엇보다 "팀을 못 찾았다"는 의미가 상태코드에 드러나지 않습니다. 공통 예외 구조를 만들었다면 표준 예외를 던지는 지점이 남아 있지 않은지 함께 확인해야 한다는 걸 이번에 정리했습니다.

5. Selenium 크롤링과 WebSocket 실시간 채팅

상황 KBO 사이트가 JavaScript(PostBack) 기반이라 Jsoup만으로는 경기 일정 데이터를 가져올 수 없었고, 실시간 채팅에는 로그인하지 않은 사용자의 연결을 막을 방법이 필요했습니다.

해결 Selenium으로 크롤링을 구현했고, 채팅은 STOMP 기반으로 붙이면서 WebSocket CONNECT 단계에서 JWT를 검증하는 인터셉터를 적용했습니다.

java
if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
    String token = accessor.getFirstNativeHeader("Authorization");
    if (token == null || !jwtTokenProvider.validateToken(token)) {
        throw new MessagingException("인증에 실패했습니다.");
    }
}

결과 Jsoup으로는 불가능했던 동적 페이지 크롤링이 가능해졌고, 인증되지 않은 사용자의 WebSocket 연결이 차단되는 것을 확인했습니다.

다음 계획
 모든 커스텀 예외가 BusinessException을 상속하는지 검증하는 회귀 테스트 추가
 N+1 개선 전/후 정확한 쿼리 횟수 측정 및 기재
 joinMatching 동시 참가 시나리오에 대한 멀티스레드 테스트 추가
 채팅방 목록 + 안 읽은 메시지 수(unread count) API 구현
 CI(GitHub Actions)로 PR 시 테스트 자동 실행
