package dooya.see.domain.post.search;

import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.PostTestDataGenerator;
import dooya.see.domain.post.dto.PostSearchRequest;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
record PostSearchPerfomanceTest(PostRepository postRepository, PostTestDataGenerator dataGenerator, EntityManager entityManager) {
    private static boolean dataInitialized = false;

    @BeforeAll
    void initTestData() {
        long count = postRepository.count();
        if (count < 1_000_000) {
            log.info("📦 현재 데이터: {}건 → 100만 건으로 생성 시작", count);
            dataGenerator.generateMillionTestData();
        } else {
            log.info("✅ 기존 데이터 사용: {}건", count);
        }
    }

    @Test
    @Order(1)
    void 데이터_초기화_확인() {
        long count = postRepository.count();
        log.info("현재 총 게시글 수: {} 건", count);

        assertTrue(count >= 100_000, "테스트 데이터가 충분하지 않습니다. 최소 10만건 필요");
        assertTrue(dataInitialized, "테스트 데이터 초기화 실패");
    }

    @Test
    @Order(2)
    void 성능_측정_1_키워드_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("테스트")
                .build();

        PerformanceResult result  = measurePerformance(
                "키워드 검색 (LIKE $keyword%)",
                request,
                5
        );

        log.warn("⚠️ 현재 성능: {} ms", result.avgTimeMs());
        if (result.avgTimeMs() > 1000) {
            log.error("❌ 성능 기준 미달: {} ms (목표: 1000ms 이하)", result.avgTimeMs());
        }
    }

    @Test
    @Order(3)
    void 성능_측정_2_카테고리_키워드_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("개발")
                .category(PostCategory.TECH)
                .build();

        PerformanceResult result = measurePerformance(
                "카테고리 + 키워드",
                request,
                5
        );

        assertTrue(result.avgTimeMs() < 2000, "카테고리 필터링 성능이 너무 느립니다: " + result.avgTimeMs() + "ms");
    }

    @Test
    @Order(4)
    void 성능_측정_3_작성자_검색_인덱스_활용() {
        PostSearchRequest request = PostSearchRequest.builder()
                .memberId(1L)
                .build();

        PerformanceResult result = measurePerformance(
                "작성자 검색",
                request,
                5
        );

        // 작성자 검색은 인덱스가 있으면 매우 빨라야 함
        if (result.avgTimeMs() > 100) {
            log.warn("⚠️ 작성자 검색이 느립니다. 인덱스 확인 필요: {} ms",
                    result.avgTimeMs());
        }
    }

    @Test
    @Order(5)
    void 성능_측정_4_날짜_범위_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .fromDate(LocalDateTime.now().minusDays(30))
                .toDate(LocalDateTime.now())
                .build();

        PerformanceResult result = measurePerformance(
                "날짜 범위 검색",
                request,
                5
        );

        if (result.avgTimeMs() > 500) {
            log.warn("⚠️ 날짜 범위 검색이 느립니다. 인덱스 확인 필요: {} ms",
                    result.avgTimeMs());
        }
    }

    @Test
    @Order(6)
    void 성능_측정_5_제목_키워드_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .titleKeyword("공지")
                .build();

        measurePerformance("제목 키워드 검색", request, 5);
    }

    @Test
    @Order(7)
    void 성능_측정_6_본문_키워드_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .contentKeyword("긴급")
                .build();

        measurePerformance("본문 키워드 검색", request, 5);
    }

    @Test
    @Order(8)
    void 성능_측정_7_복합_조건_검색_최적화된_케이스() {
        PostSearchRequest request = PostSearchRequest.builder()
                .memberId(1L)
                .category(PostCategory.TECH)
                .fromDate(LocalDateTime.now().minusDays(30))
                .status(PostStatus.PUBLISHED)
                .build();

        PerformanceResult result = measurePerformance(
                "복합 조건 검색 (최적)",
                request,
                5
        );

        // 복합 조건은 빨라야 함
        assertTrue(result.avgTimeMs() < 200,
                "복합 조건 검색 성능이 기대치보다 느립니다: " + result.avgTimeMs() + "ms");
    }

    @Test
    @Order(9)
    void 성능_측정_8_상태별_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .status(PostStatus.PUBLISHED)
                .build();

        measurePerformance("상태별 검색", request, 3);
    }

    @Test
    @Order(10)
    void 전쳬_시나리오_성능_비교_리포트() {
        log.info("\n" + "=".repeat(90));
        log.info("📊 전체 시나리오 성능 비교 리포트");
        log.info("=".repeat(90));

        List<TestScenario> scenarios = List.of(
                new TestScenario(
                        "1. 키워드만 (최악 - Full Scan)",
                        PostSearchRequest.builder().keyword("테스트").build()
                ),
                new TestScenario(
                        "2. 제목 키워드 (LIKE %keyword%)",
                        PostSearchRequest.builder().titleKeyword("공지").build()
                ),
                new TestScenario(
                        "3. 본문 키워드 (LIKE %keyword%)",
                        PostSearchRequest.builder().contentKeyword("긴급").build()
                ),
                new TestScenario(
                        "4. 카테고리만 (인덱스 활용 가능)",
                        PostSearchRequest.builder().category(PostCategory.TECH).build()
                ),
                new TestScenario(
                        "5. 작성자만 (인덱스 활용 가능)",
                        PostSearchRequest.builder().memberId(1L).build()
                ),
                new TestScenario(
                        "6. 날짜 범위만 (인덱스 활용 가능)",
                        PostSearchRequest.builder()
                                .fromDate(LocalDateTime.now().minusDays(7))
                                .build()
                ),
                new TestScenario(
                        "7. 상태만 (인덱스 활용 가능)",
                        PostSearchRequest.builder().status(PostStatus.PUBLISHED).build()
                ),
                new TestScenario(
                        "8. 카테고리 + 키워드 (중간)",
                        PostSearchRequest.builder()
                                .keyword("개발")
                                .category(PostCategory.TECH)
                                .build()
                ),
                new TestScenario(
                        "9. 날짜 + 카테고리 (좋음)",
                        PostSearchRequest.builder()
                                .category(PostCategory.TECH)
                                .fromDate(LocalDateTime.now().minusDays(30))
                                .build()
                ),
                new TestScenario(
                        "10. 복합 조건 (최선 - 여러 인덱스)",
                        PostSearchRequest.builder()
                                .memberId(1L)
                                .category(PostCategory.TECH)
                                .fromDate(LocalDateTime.now().minusDays(30))
                                .status(PostStatus.PUBLISHED)
                                .build()
                )
        );

        List<PerformanceResult> results = new ArrayList<>();

        for (TestScenario scenario : scenarios) {
            entityManager.clear(); // 캐시 초기화

            long totalTime = 0;
            int resultCount = 0;

            // 3회 측정 후 평균
            for (int i = 0; i < 3; i++) {
                long start = System.currentTimeMillis();
                List<Post> posts = postRepository.search(scenario.request());
                long elapsed = System.currentTimeMillis() - start;

                totalTime += elapsed;
                resultCount = posts.size();

                // JVM 워밍업
                if (i == 0) {
                    Thread.yield();
                }
            }

            long avgTime = totalTime / 3;
            results.add(new PerformanceResult(scenario.name(), avgTime, resultCount));
        }

        // 결과 출력
        log.info("\n" + "=".repeat(90));
        log.info(String.format("| %-55s | %-12s | %-12s |", "시나리오", "평균 시간", "결과 수"));
        log.info("=".repeat(90));

        for (PerformanceResult result : results) {
            String emoji = getPerformanceEmoji(result.avgTimeMs());
            log.info(String.format("| %-55s | %s %8dms | %10d건 |",
                    result.scenario(),
                    emoji,
                    result.avgTimeMs(),
                    result.resultCount()));
        }

        log.info("=".repeat(90));

        // 통계 정보
        long totalAvg = results.stream()
                .mapToLong(PerformanceResult::avgTimeMs)
                .sum() / results.size();

        PerformanceResult fastest = results.stream()
                .min((a, b) -> Long.compare(a.avgTimeMs(), b.avgTimeMs()))
                .orElseThrow();

        PerformanceResult slowest = results.stream()
                .max((a, b) -> Long.compare(a.avgTimeMs(), b.avgTimeMs()))
                .orElseThrow();

        log.info("\n📈 통계 요약:");
        log.info("  - 전체 평균: {} ms", totalAvg);
        log.info("  - 가장 빠른 쿼리: {} - {} ms ✅",
                fastest.scenario(), fastest.avgTimeMs());
        log.info("  - 가장 느린 쿼리: {} - {} ms ⚠️",
                slowest.scenario(), slowest.avgTimeMs());

        // 성능 문제 경고
        long slowQueries = results.stream()
                .filter(r -> r.avgTimeMs() > 1000)
                .count();

        if (slowQueries > 0) {
            log.error("\n🚨 성능 경고!");
            log.error("  - 1초 이상 소요되는 쿼리: {} 개", slowQueries);
            log.error("  - 성능 개선이 시급합니다!");

            results.stream()
                    .filter(r -> r.avgTimeMs() > 1000)
                    .forEach(r -> log.error("    ❌ {} - {} ms",
                            r.scenario(), r.avgTimeMs()));
        } else {
            log.info("\n✅ 모든 쿼리가 1초 이내로 응답합니다.");
        }

        log.info("\n" + "=".repeat(90) + "\n");

        // 개선 권장사항
        printOptimizationRecommendations(results);
    }

    /**
     * 성능 측정 헬퍼 메서드
     */
    private PerformanceResult measurePerformance(
            String testName,
            PostSearchRequest request,
            int iterations
    ) {
        log.info("\n" + "=".repeat(70));
        log.info("🔍 테스트: {}", testName);
        log.info("=".repeat(70));

        List<Long> executionTimes = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < iterations; i++) {
            // 캐시 초기화
            entityManager.clear();

            // JVM 워밍업을 위한 대기
            if (i == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            long startTime = System.nanoTime();
            List<Post> results = postRepository.search(request);
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // 나노초 -> 밀리초

            executionTimes.add(executionTime);
            resultCount = results.size();

            log.debug("  반복 {} - 실행 시간: {} ms", i + 1, executionTime);
        }

        // 통계 계산
        long sum = executionTimes.stream().mapToLong(Long::longValue).sum();
        long avg = sum / iterations;
        long min = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        log.info("📊 결과:");
        log.info("  - 평균 실행 시간: {} ms {}", avg, getPerformanceEmoji(avg));
        log.info("  - 최소 실행 시간: {} ms", min);
        log.info("  - 최대 실행 시간: {} ms", max);
        log.info("  - 검색 결과 수: {} 건", resultCount);

        // 성능 평가
        if (avg > 1000) {
            log.error("  ⚠️  성능 경고: 평균 실행 시간이 1초를 초과했습니다!");
        } else if (avg > 500) {
            log.warn("  ⚠️  주의: 평균 실행 시간이 500ms를 초과했습니다.");
        } else if (avg < 100) {
            log.info("  ✅ 우수: 매우 빠른 응답 속도입니다.");
        }

        log.info("=".repeat(70) + "\n");

        return new PerformanceResult(testName, avg, resultCount);
    }

    /**
     * 성능에 따른 이모지 반환
     */
    private String getPerformanceEmoji(long timeMs) {
        if (timeMs < 50) return "🚀";      // 매우 빠름
        if (timeMs < 100) return "✅";     // 빠름
        if (timeMs < 500) return "😊";     // 양호
        if (timeMs < 1000) return "⚠️";    // 주의
        return "❌";                        // 느림
    }

    /**
     * 개선 권장사항 출력
     */
    private void printOptimizationRecommendations(List<PerformanceResult> results) {
        log.info("💡 성능 개선 권장사항:");
        log.info("=".repeat(90));

        boolean hasSlowKeywordSearch = results.stream()
                .anyMatch(r -> r.scenario().contains("키워드") && r.avgTimeMs() > 1000);

        if (hasSlowKeywordSearch) {
            log.info("\n1. 키워드 검색 개선:");
            log.info("   ❌ 현재: LIKE '%keyword%' 사용 (Full Table Scan)");
            log.info("   ✅ 권장: Full-Text Search 인덱스 사용");
            log.info("   ✅ 대안: Elasticsearch 같은 검색 엔진 도입");
            log.info("   ✅ 임시: LIKE 'keyword%' 형태로 변경 (앞부분 고정)");
        }

        boolean hasSlowDateSearch = results.stream()
                .anyMatch(r -> r.scenario().contains("날짜") && r.avgTimeMs() > 200);

        if (hasSlowDateSearch) {
            log.info("\n2. 날짜 검색 개선:");
            log.info("   ✅ createdAt 컬럼에 인덱스 추가");
            log.info("   ✅ 복합 인덱스 고려: (status, createdAt)");
        }

        log.info("\n3. 일반적인 개선 방안:");
        log.info("   ✅ 자주 사용되는 조건에 인덱스 추가");
        log.info("   ✅ 페이지네이션 적용 (LIMIT, OFFSET)");
        log.info("   ✅ 캐싱 전략 적용 (Redis)");
        log.info("   ✅ 읽기 전용 복제본 사용");

        log.info("\n" + "=".repeat(90));
    }

    // 헬퍼 클래스
    record TestScenario(String name, PostSearchRequest request) {}
    record PerformanceResult(String scenario, long avgTimeMs, int resultCount) {}
}
