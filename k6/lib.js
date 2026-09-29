import http from 'k6/http';

export const BASE = __ENV.BASE_URL || 'http://localhost:8080';
export const PASSWORD = 'Passw0rd!';
export const JSON_HEADERS = { 'Content-Type': 'application/json' };

export function auth(token) {
  return { headers: { ...JSON_HEADERS, Authorization: `Bearer ${token}` } };
}

// 서버는 LocalDateTime(시간대 없는 문자열)을 받는다. 내일 이 시각이면 충분하다.
export function tomorrow() {
  return new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 19);
}

// userId 4~20자, 닉네임 2~10자 규칙에 맞춘다
export function signUp(key) {
  const userId = `k${key}`.slice(0, 20);
  const body = {
    userId,
    pw: PASSWORD,
    nickname: `n${key}`.slice(0, 10),
    name: '부하테스트',
    email: `${userId}@k6.test`,
  };
  const res = http.post(`${BASE}/user/signup`, JSON.stringify(body), { headers: JSON_HEADERS });
  return { userId, res };
}

export function login(userId, password = PASSWORD) {
  return http.post(`${BASE}/user/login`, JSON.stringify({ userId, password }), { headers: JSON_HEADERS });
}

export function signUpAndLogin(key) {
  const { userId } = signUp(key);
  const res = login(userId);
  return { userId, token: res.status === 200 ? res.json('accessToken') : null };
}

// 실행마다 겹치지 않는 5자리 접두어
export function runId() {
  return Date.now().toString(36).slice(-5);
}
