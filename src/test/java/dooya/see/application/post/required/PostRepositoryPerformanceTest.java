package dooya.see.application.post.required;

import dooya.see.application.post.PostTestDataGenerator;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.dto.PostSearchRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PostTestDataGenerator.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostRepositoryPerformanceTest {
    private static final Logger log = LoggerFactory.getLogger(PostRepositoryPerformanceTest.class);
    private static final int DATASET_SIZE = 100_000;
    private static final int ITERATIONS = 3;
    private static final double PERFORMANCE_THRESHOLD = 1.5;

    private final PostRepository postRepository;
    private final PostTestDataGenerator dataGenerator;
    private final EntityManager entityManager;

    PostRepositoryPerformanceTest(PostRepository postRepository,
                                  PostTestDataGenerator dataGenerator,
                                  EntityManager entityManager) {
        this.postRepository = postRepository;
        this.dataGenerator = dataGenerator;
        this.entityManager = entityManager;
    }

    private void ensureDatasetPrepared() {
        long existingCount = postRepository.count();
        if (existingCount >= DATASET_SIZE) {
            log.info("[perf] dataset already prepared ({} posts)", existingCount);
            return;
        }

        int remaining = Math.toIntExact(DATASET_SIZE - existingCount);
        log.info("[perf] preparing dataset: existing={}, target={}, generating={}",
                existingCount, DATASET_SIZE, remaining);

        long start = System.nanoTime();
        dataGenerator.generateTestData(remaining);
        entityManager.clear();
        long durationMillis = nanosToMillis(System.nanoTime() - start);

        long total = postRepository.count();
        log.info("[perf] dataset ready in {} ms (total posts: {})", durationMillis, total);
    }

    @Test
    void 단계별_검색_성능을_측정하고_비교한다() {
        ensureDatasetPrepared();
        entityManager.clear();

        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Performance")
                .status(PostStatus.PUBLISHED)
                .build();
        Pageable pageable = PageRequest.of(0, 50);

        List<PerformanceMetric> metrics = new ArrayList<>();

        PerformanceMetric baseline = measure("baseline", () -> postRepository.search(request));
        PerformanceMetric paginated = measure("baseline+pagination", () -> postRepository.searchWithPagination(request, pageable));
        PerformanceMetric optimized = measure("optimized-ordering", () -> postRepository.searchWithOptimizedConditions(request, pageable));

        metrics.add(baseline);
        metrics.add(paginated);
        metrics.add(optimized);

        assertThat(paginated.duration().toNanos())
                .as("Pagination should not be significantly slower than baseline")
                .isLessThanOrEqualTo((long) (baseline.duration().toNanos() * PERFORMANCE_THRESHOLD));
        assertThat(optimized.duration().toNanos())
                .as("Optimized query should stay within acceptable range compared to baseline")
                .isLessThanOrEqualTo((long) (baseline.duration().toNanos() * PERFORMANCE_THRESHOLD));

        if (supportsMySqlFullText()) {
            PerformanceMetric fullText = measure("mysql-fulltext", () -> postRepository.searchWithFullTextIndex(
                    PostStatus.PUBLISHED.name(),
                    null,
                    null,
                    null,
                    null,
                    "Performance",
                    pageable
            ));
            metrics.add(fullText);

            assertThat(fullText.resultSize())
                    .as("Full-text search should return the same number of posts as the paginated JPQL query")
                    .isEqualTo(paginated.resultSize());
        }

        metrics.forEach(metric ->
                log.info("[perf] collected '{}' metric: avg {} ms (rows: {})",
                        metric.label(),
                        formatMillis(metric.duration()),
                        metric.resultSize())
        );

        logSummary(metrics);
    }

    private PerformanceMetric measure(String label, Supplier<Object> query) {
        entityManager.clear();
        query.get(); // warm-up

        long totalNanos = 0;
        int lastSize = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            entityManager.clear();
            long start = System.nanoTime();
            Object result = query.get();
            long iterationNanos = System.nanoTime() - start;
            totalNanos += iterationNanos;
            lastSize = extractSize(result);
            log.info("[perf] '{}' iteration {} took {} ms (rows: {})",
                    label, i + 1, formatMillis(iterationNanos), lastSize);
        }

        long averageNanos = Math.max(1L, totalNanos / ITERATIONS);
        assertThat(lastSize)
                .as("Query '%s' should return at least one row", label)
                .isPositive();

        log.info("[perf] '{}' 평균 {} ms over {} runs (rows: {})",
                label, formatMillis(averageNanos), ITERATIONS, lastSize);

        return new PerformanceMetric(label, Duration.ofNanos(averageNanos), lastSize);
    }

    private int extractSize(Object result) {
        if (result instanceof Page<?> page) {
            return page.getContent().size();
        }
        if (result instanceof List<?> list) {
            return list.size();
        }
        throw new IllegalArgumentException("지원하지 않는 반환 타입: " + result.getClass());
    }

    private boolean supportsMySqlFullText() {
        Object dialect = entityManager.getEntityManagerFactory()
                .getProperties()
                .get("hibernate.dialect");
        return dialect != null && dialect.toString().toLowerCase(Locale.ROOT).contains("mysql");
    }

    private long nanosToMillis(long nanos) {
        return Duration.ofNanos(nanos).toMillis();
    }

    private String formatMillis(long nanos) {
        return String.format(Locale.US, "%.2f", nanos / 1_000_000.0);
    }

    private String formatMillis(Duration duration) {
        return formatMillis(duration.toNanos());
    }

    private void logSummary(List<PerformanceMetric> metrics) {
        if (metrics.isEmpty())
            return;

        Duration baselineDuration = metrics.get(0).duration();
        log.info("[perf][summary] ----------------------------------------------------");
        metrics.forEach(metric -> {
            double ratio = computeRelativeRatio(baselineDuration, metric.duration());
            log.info("[perf][summary] %-20s | avg %8s ms | rel %5.2fx | rows %6d",
                    metric.label(),
                    formatMillis(metric.duration()),
                    ratio,
                    metric.resultSize());
        });
        log.info("[perf][summary] baseline: '{}' 기준", metrics.get(0).label());
    }

    private double computeRelativeRatio(Duration baseline, Duration current) {
        if (baseline.isZero()) {
            return 1.0;
        }
        return (double) current.toNanos() / baseline.toNanos();
    }

    private record PerformanceMetric(String label, Duration duration, int resultSize) {
    }
}
