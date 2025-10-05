package dooya.see.domain.post.search;

import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostTestDataGenerator;
import dooya.see.domain.post.dto.PostSearchRequest;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2단계: 조건 순서 최적화 성능 비교 테스트
 * - 목표: 인덱스가 활용 가능한 조건을 먼저 평가하도록 WHERE 절 순서 변경
 * - 기대효과: 불필요한 Full Scan 감소 및 실행 시간 최소 1.5~2배 개선
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
record Step2ConditionOrderTest(PostRepository postRepository, PostTestDataGenerator dataGenerator, EntityManager entityManager) {

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
    @DisplayName("2단계: 조건 순서 최적화 적용 전후 비교")
    void BEFORE_VS_AFTER_조건순서최적화_비교() {
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("테스트")
                .category(dooya.see.domain.post.PostCategory.GENERAL)
                .status(dooya.see.domain.post.PostStatus.PUBLISHED)
                .build();

        log.info("\n" + "=".repeat(80));
        log.info("2단계: 조건 순서 최적화 성능 비교");
        log.info("=".repeat(80));

        // === BEFORE: 기존 순서 (LIKE 조건 우선) ===
        entityManager.clear();
        System.gc();

        long beforeStart = System.currentTimeMillis();
        Page<Post> beforePage = postRepository.searchWithPagination(
                request,
                PageRequest.of(0, 20, Sort.by("metaData.createdAt").descending())
        );
        long beforeTime = System.currentTimeMillis() - beforeStart;

        log.info("\n📦 BEFORE (LIKE 우선 조건)");
        log.info("  ⏱️  실행 시간: {} ms", beforeTime);
        log.info("  📊 조회 건수: {}", beforePage.getContent().size());

        // === AFTER: 조건 순서 최적화 (인덱스 조건 우선) ===
        entityManager.clear();
        System.gc();

        long afterStart = System.currentTimeMillis();
        Page<Post> afterPage = postRepository.searchWithOptimizedConditions(
                // 내부적으로 WHERE 절 순서를 변경한 버전으로 수정되었다고 가정
                request,
                PageRequest.of(0, 20, Sort.by("metaData.createdAt").descending())
        );
        long afterTime = System.currentTimeMillis() - afterStart;

        log.info("\n📄 AFTER (인덱스 우선 조건)");
        log.info("  ⏱️  실행 시간: {} ms", afterTime);
        log.info("  📊 조회 건수: {}", afterPage.getContent().size());
        log.info("  📈 전체 건수: {}", afterPage.getTotalElements());
        log.info("  📑 총 페이지: {}", afterPage.getTotalPages());

        // === 개선 효과 계산 ===
        double speedImprovement = (double) beforeTime / afterTime;

        log.info("\n✨ 개선 효과");
        log.info("  🚀 속도 개선: {}", String.format("%.1f배 빠름!", speedImprovement));
        log.info("  ✅ 인덱스 활용 향상으로 Full Scan 비율 감소 예상");

        log.info("\n" + "=".repeat(80) + "\n");

        // === 검증 ===
        assertTrue(afterTime < beforeTime,
                "조건 순서 최적화 후 쿼리가 더 느립니다!");
        assertTrue(speedImprovement >= 1.5,
                "최소 1.5배 이상 개선되어야 합니다!");
    }
}