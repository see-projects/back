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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
record Step3LikePatternTest(PostRepository postRepository, PostTestDataGenerator dataGenerator, EntityManager entityManager) {

    @BeforeAll
    void initTestData() {
        long count = postRepository.count();
        if (count < 100_000) {
            log.info("테스트 데이터를 생성합니다...");
            dataGenerator.generateRecommendedTestData();
        } else {
            log.info("기존 데이터 사용: {} 건", count);
        }
    }

    @Test
    @DisplayName("3단계: LIKE 패턴 최적화 적용 전후 비교")
    void BEFORE_VS_AFTER_LIKE패턴_최적화_비교() {
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("테스트") // 앞부분 일치 검색 대상
                .category(PostCategory.GENERAL)
                .status(PostStatus.PUBLISHED)
                .build();

        log.info("\n" + "=".repeat(80));
        log.info("3단계: LIKE 패턴 최적화 성능 비교");
        log.info("=".repeat(80));

        // === BEFORE: 2단계 인덱스 우선 조건 (LIKE '%keyword%') ===
        entityManager.clear();
        System.gc();

        long beforeStart = System.currentTimeMillis();
        Page<Post> beforePage = postRepository.searchWithOptimizedConditions(
                request,
                PageRequest.of(0, 20, Sort.by("metaData.createdAt").descending())
        );
        long beforeTime = System.currentTimeMillis() - beforeStart;

        log.info("\n📦 BEFORE (LIKE '%keyword%')");
        log.info("  ⏱️  실행 시간: {} ms", beforeTime);
        log.info("  📊 조회 건수: {}", beforePage.getContent().size());

        // === AFTER: 3단계 LIKE 패턴 개선 (LIKE 'keyword%') ===
        entityManager.clear();
        System.gc();

        long afterStart = System.currentTimeMillis();
        Page<Post> afterPage = postRepository.searchWithLikeOptimization(
                request,
                PageRequest.of(0, 20, Sort.by("metaData.createdAt").descending())
        );
        long afterTime = System.currentTimeMillis() - afterStart;

        log.info("\n📄 AFTER (LIKE 'keyword%')");
        log.info("  ⏱️  실행 시간: {} ms", afterTime);
        log.info("  📊 조회 건수: {}", afterPage.getContent().size());
        log.info("  📈 전체 건수: {}", afterPage.getTotalElements());
        log.info("  📑 총 페이지: {}", afterPage.getTotalPages());

        // === 개선 효과 ===
        double speedImprovement = (double) beforeTime / afterTime;

        log.info("\n✨ 개선 효과");
        log.info("  🚀 속도 개선: {}", String.format("%.1f배 빠름!", speedImprovement));
        log.info("  ✅ LIKE 패턴 변경으로 인덱스 Prefix 탐색 활성화");

        log.info("\n" + "=".repeat(80) + "\n");

        // 검증
        assertTrue(afterTime < beforeTime, "LIKE 패턴 최적화가 더 느립니다!");
        assertTrue(speedImprovement >= 1.7, "최소 1.7배 이상 개선되어야 합니다!");
    }
}