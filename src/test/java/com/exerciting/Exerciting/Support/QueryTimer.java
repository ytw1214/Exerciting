package com.exerciting.Exerciting.Support;

/**
 * 테스트 공용 성능 측정 유틸.
 * 첫 실행에는 커넥션 준비, JIT 컴파일 등 측정 대상과 무관한 시간이 섞이므로
 * 워밍업을 먼저 돌린 뒤 여러 번 측정한 평균을 사용한다.
 */
public final class QueryTimer {

    public static final int WARMUP = 5;
    public static final int REPEAT = 20;

    private QueryTimer() {
    }

    public static double averageMillis(Runnable query) {
        for (int i = 0; i < WARMUP; i++) {
            query.run();
        }

        long totalNanos = 0;
        for (int i = 0; i < REPEAT; i++) {
            long start = System.nanoTime();
            query.run();
            totalNanos += System.nanoTime() - start;
        }
        return totalNanos / (double) REPEAT / 1_000_000;
    }

    public static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
