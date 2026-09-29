// 핵심 흐름 스모크 테스트: 고친 결함이 실제 서버에서 고쳐졌는지 한 번에 확인한다.
//
//   k6 run -e GAME_ID=1 k6/smoke.js
//
// 모든 check가 통과해야 종료 코드 0. 하나라도 실패하면 어떤 단계인지 출력된다.
import http from 'k6/http';
import { check, group, fail } from 'k6';
import { BASE, JSON_HEADERS, auth, tomorrow, signUp, login, runId } from './lib.js';

const GAME_ID = Number(__ENV.GAME_ID);

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: { checks: ['rate==1.0'] },
};

function expect(res, name, status) {
  const ok = check(res, { [`${name} → ${status}`]: (r) => r.status === status });
  if (!ok) console.error(`[실패] ${name}: 기대 ${status}, 실제 ${res.status} ${res.body}`);
  return ok;
}

export default function () {
  if (!GAME_ID) fail('GAME_ID가 필요합니다. 예) k6 run -e GAME_ID=1 k6/smoke.js');
  const run = runId();
  let hostToken, guestToken, guestId, matchingId;

  group('회원가입·로그인', () => {
    const host = signUp(`${run}a`);
    const guest = signUp(`${run}b`);
    expect(host.res, '가입(호스트)', 200);
    expect(guest.res, '가입(게스트)', 200);
    hostToken = login(host.userId).json('accessToken');
    guestToken = login(guest.userId).json('accessToken');
    guestId = guest.userId;
    expect(http.get(`${BASE}/user/me`, auth(hostToken)), '내 정보', 200);
  });

  group('경기 조회', () => {
    const res = http.get(`${BASE}/api/v1/games`);
    check(res, { '날짜 없이 경기 조회 → 2xx (예전: 500)': (r) => r.status === 200 || r.status === 204 });
  });

  group('매칭 참가·이탈', () => {
    const created = http.post(`${BASE}/api/v1/matching`, JSON.stringify({
      title: '스모크 테스트', description: '핵심 흐름 확인', maxPerson: 4, meetTime: tomorrow(), gameId: GAME_ID,
    }), auth(hostToken));
    expect(created, '매칭 생성 (예전: 호스트 이중 저장으로 실패)', 200);
    matchingId = created.json('matchingId');

    expect(http.post(`${BASE}/api/v1/matching/${matchingId}/join`, null, auth(guestToken)), '참가', 200);
    expect(http.post(`${BASE}/api/v1/matching/${matchingId}/join`, null, auth(guestToken)), '중복 참가', 409);
    const joined = http.get(`${BASE}/api/v1/participant/${matchingId}`, auth(hostToken));
    check(joined, { '참가자 2명': (r) => r.status === 200 && r.json().length === 2 });

    expect(http.post(`${BASE}/api/v1/matching/abc/join`, null, auth(guestToken)), '잘못된 경로 변수 (예전: 500)', 400);

    expect(http.del(`${BASE}/api/v1/matching/${matchingId}/leave`, null, auth(guestToken)), '이탈', 200);
    const afterLeave = http.get(`${BASE}/api/v1/participant/${matchingId}`, auth(hostToken));
    check(afterLeave, { '나간 사람은 참가자 목록에서 빠짐': (r) => r.status === 200 && r.json().length === 1 });
  });

  group('권한', () => {
    expect(http.post(`${BASE}/api/ranks/sync`, null, auth(hostToken)), '일반 사용자의 순위 동기화', 403);
    expect(http.patch(`${BASE}/user/me`, JSON.stringify({ pw: 'NewPassw0rd!' }), auth(guestToken)),
      '현재 비밀번호 없이 비밀번호 변경', 400);
  });

  group('취소·탈퇴', () => {
    expect(http.del(`${BASE}/user/me`, null, auth(hostToken)), '진행 중 매칭이 있는 호스트 탈퇴', 409);
    expect(http.del(`${BASE}/api/v1/matching/${matchingId}`, null, auth(hostToken)), '매칭 취소', 200);
    const list = http.get(`${BASE}/api/v1/matching?size=100`, auth(hostToken));
    check(list, {
      '취소된 매칭은 목록에 없음': (r) => r.status === 200 && !r.json('content').some((m) => m.matchingId === matchingId),
    });

    expect(http.del(`${BASE}/user/me`, null, auth(guestToken)), '게스트 탈퇴 (예전: FK 위반 500)', 200);
    expect(login(guestId), '탈퇴한 아이디로 로그인', 401);
    expect(http.del(`${BASE}/user/me`, null, auth(hostToken)), '매칭 취소 후 호스트 탈퇴', 200);
  });
}
