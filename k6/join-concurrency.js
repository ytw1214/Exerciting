// 실제 MySQL에서 "동시 참가 요청이 몰려도 정원을 넘지 않는다"를 확인한다.
//
//   k6 run -e GAME_ID=1 k6/join-concurrency.js
//   (옵션) -e USERS=100 -e CAPACITY=5 -e BASE_URL=http://localhost:8080
//
// setup에서 호스트 1명 + 참가자 USERS명을 가입·로그인시키고 정원 CAPACITY명 매칭을 만든 뒤,
// USERS개의 가상 사용자가 한꺼번에 같은 매칭에 참가를 누른다. 끝나면 실제 참가자 수를 확인한다.
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';
import { BASE, auth, tomorrow, signUpAndLogin, runId } from './lib.js';

const GAME_ID = Number(__ENV.GAME_ID);
const USERS = Number(__ENV.USERS || 100);
const CAPACITY = Number(__ENV.CAPACITY || 5);

const joinSuccess = new Counter('join_success');
const joinRejected = new Counter('join_rejected_409');
const joinUnexpected = new Counter('join_unexpected');

export const options = {
  setupTimeout: '300s',
  scenarios: {
    burst: { executor: 'per-vu-iterations', vus: USERS, iterations: 1, maxDuration: '60s' },
  },
  thresholds: {
    join_unexpected: ['count==0'],
    checks: ['rate==1.0'],
  },
};

export function setup() {
  if (!GAME_ID) {
    throw new Error('GAME_ID가 필요합니다. 예) k6 run -e GAME_ID=1 k6/join-concurrency.js');
  }
  const run = runId();
  const host = signUpAndLogin(`${run}h`);
  const created = http.post(`${BASE}/api/v1/matching`, JSON.stringify({
    title: 'k6 동시 참가', description: '정원 보장 부하 테스트',
    maxPerson: CAPACITY, meetTime: tomorrow(), gameId: GAME_ID,
  }), auth(host.token));
  check(created, { '매칭 생성 200': (r) => r.status === 200 });

  const tokens = [];
  for (let i = 0; i < USERS; i++) {
    tokens.push(signUpAndLogin(`${run}${String(i).padStart(3, '0')}`).token);
  }
  return { matchingId: created.json('matchingId'), hostToken: host.token, tokens };
}

export default function (data) {
  const res = http.post(`${BASE}/api/v1/matching/${data.matchingId}/join`, null, auth(data.tokens[__VU - 1]));
  if (res.status === 200) {
    joinSuccess.add(1);
  } else if (res.status === 409) {
    joinRejected.add(1);
  } else {
    joinUnexpected.add(1);
    console.error(`예상 밖 응답 ${res.status}: ${res.body}`);
  }
}

export function teardown(data) {
  const res = http.get(`${BASE}/api/v1/participant/${data.matchingId}`, auth(data.hostToken));
  const participants = res.status === 200 ? res.json().length : 0;
  console.log(`정원 ${CAPACITY}명(호스트 포함) / 동시 요청 ${USERS}건 → 실제 참가자 ${participants}명`);
  check(participants, { '실제 참가자 수 == 정원': (n) => n === CAPACITY });
}
