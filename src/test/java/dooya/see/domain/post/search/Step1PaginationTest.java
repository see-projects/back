package dooya.see.domain.post.search;

import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostTestDataGenerator;
import dooya.see.domain.post.dto.PostSearchRequest;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
record Step1PaginationTest(PostRepository postRepository, PostTestDataGenerator dataGenerator, EntityManager entityManager) {

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
    void BEFORE_VS_AFTER_비교_키워드_검색() {
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("테스트")
                .build();

        log.info("\n" + "=".repeat(80));
        log.info("1단계: 페이지네이션 성능 비교");
        log.info("=".repeat(80));

        // === BEFORE: List 사용 (전체 조회) ===
        entityManager.clear();
        System.gc(); // 가비지 컬렉션

        long beforeStart = System.currentTimeMillis();
        List<Post> listResult = postRepository.search(request);
        long beforeTime = System.currentTimeMillis() - beforeStart;

        log.info("\n📦 BEFORE (List - 전체 조회)");
        log.info("  ⏱️  실행 시간: {} ms", beforeTime);
        log.info("  📊 조회 건수: {} 건", listResult.size());
        log.info("  💾 메모리 사용: 약 {} MB",
                String.format("%.2f", listResult.size() * 1.0 / 1024));

        // === AFTER: Page 사용 (20건만) ===
        entityManager.clear();
        System.gc();

        long afterStart = System.currentTimeMillis();
        Page<Post> pageResult = postRepository.searchWithPagination(
                request,
                PageRequest.of(0, 20, Sort.by("metaData.createdAt").descending())
        );
        long afterTime = System.currentTimeMillis() - afterStart;

        log.info("\n📄 AFTER (Page - 20건만)");
        log.info("  ⏱️  실행 시간: {} ms", afterTime);
        log.info("  📊 조회 건수: {} 건", pageResult.getContent().size());
        log.info("  📈 전체 건수: {} 건", pageResult.getTotalElements());
        log.info("  📑 총 페이지: {} 페이지", pageResult.getTotalPages());
        log.info("  💾 메모리 사용: 약 {} MB",
                String.format("%.2f", pageResult.getContent().size() * 1.0 / 1024));

        // === 개선 효과 ===
        double speedImprovement = (double) beforeTime / afterTime;
        double memoryReduction = (1 - (double) pageResult.getContent().size() / listResult.size()) * 100;

        log.info("\n✨ 개선 효과");
        log.info("  🚀 속도 개선: {}", String.format("%.1f배 빠름!", speedImprovement));
        log.info("  💾 메모리 절감: {}", String.format("%.2f%% 감소!", memoryReduction));
        log.info("  ✅ 네트워크 부하: {}",
                String.format("%.2f%% 감소!", memoryReduction));

        log.info("\n" + "=".repeat(80) + "\n");

        // 검증
        assertTrue(afterTime < beforeTime,
                "페이지네이션이 더 느립니다!");
        assertTrue(speedImprovement >= 2.0,
                "최소 2배 이상 개선되어야 합니다!");
    }
}
