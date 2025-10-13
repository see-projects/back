package dooya.see.application.post;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostContent;
import dooya.see.domain.post.PostMetaData;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.Tag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 💡 성능 테스트용 게시글 데이터 생성기 (영문 데이터 버전)
 * - 본문, 제목, 태그, 카테고리를 모두 영어로 구성
 * - 주석은 한글로 유지
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostTestDataGenerator {

    private static final int BATCH_SIZE = 1_000;
    private final EntityManager entityManager;
    private final Random random = new Random();

    /**
     * 지정된 개수만큼 테스트 데이터를 생성합니다.
     * 대용량(10만 건 이상) 생성 시 배치 단위로 flush/clear 수행
     */
    @Transactional
    public void generateTestData(int count) {
        log.info("=== 테스트 데이터 생성 시작: {} 건 ===", count);

        long startTime = System.currentTimeMillis();

        for (int index = 0; index < count; index++) {
            entityManager.persist(createRealisticPost(index, count));
            handleBatchProgress(index + 1, count, startTime);
        }

        completeBatch(startTime, count);
    }

    /**
     * 실제 환경을 모사한 게시글 엔티티 생성 (영문 데이터)
     */
    private Post createRealisticPost(int index, int totalCount) {
        Post post = instantiatePost();

        applyField(post, "content", new PostContent(generateTitle(index), generateBody(index)));
        applyField(post, "memberId", selectMemberId(index));
        applyField(post, "category", selectCategory(index));
        applyField(post, "status", selectStatus(index));
        applyField(post, "metaData", generateMetaData(index, totalCount));
        applyField(post, "tags", generateTags(index));

        return post;
    }

    /**
     * 게시글 제목 생성 (영문)
     */
    private String generateTitle(int index) {
        String[] prefixes = {"Performance", "Notice", "Question", "Info", "Review", "Discussion", "Development", "News"};
        String[] keywords = {"Community", "Service", "Feature", "Update", "Bug",
                "Improvement", "Suggestion", "Inquiry", "Sharing", "Performance"};

        String prefix = prefixes[index % prefixes.length];
        String keyword = keywords[(index / prefixes.length) % keywords.length];

        return String.format("[%s] %s related post #%d", prefix, keyword, index);
    }

    /**
     * 게시글 본문 생성 (영문)
     * - 실제 서비스 환경을 흉내 내도록 다양한 문장 구성
     */
    private String generateBody(int index) {
        int targetLength = 200 + (index % 2800);
        StringBuilder body = new StringBuilder();

        String[] sentences = {
                "This is a test post for performance benchmarking. ",
                "The content simulates real-world community discussions. ",
                "Various keywords are included to test search performance. ",
                "We are currently improving the search optimization process. ",
                "This article focuses on indexing, query speed, and efficiency. ",
                "Full-text search will be used to compare with LIKE queries. ",
                "Users are encouraged to share suggestions for improvement. "
        };

        while (body.length() < targetLength) {
            body.append(sentences[random.nextInt(sentences.length)]);
        }

        // 특정 단어 삽입 (검색 시 노출되도록)
        if (index % 10 == 0) body.append("Important notice about database optimization. ");
        if (index % 7 == 0) body.append("Urgent update regarding search performance. ");
        if (index % 5 == 0) body.append("Frequently asked questions about community service. ");

        return body.substring(0, Math.min(targetLength, body.length()));
    }

    /**
     * 작성자 ID 선택 (파레토 법칙 적용)
     */
    private Long selectMemberId(int index) {
        int totalUsers = 10_000;

        if (index % 5 < 4) {
            // 상위 20% 사용자가 80%의 게시글 작성
            return (long) (1 + (index % (totalUsers / 5)));
        } else {
            // 나머지 일반 사용자
            return (long) ((totalUsers / 5) + 1 + (index % (totalUsers * 4 / 5)));
        }
    }

    /**
     * 게시글 카테고리 분포 (영문)
     */
    private PostCategory selectCategory(int index) {
        int mod = index % 100;
        if (mod < 40) return PostCategory.GENERAL;
        if (mod < 65) return PostCategory.TECH;
        if (mod < 80) return PostCategory.QNA;
        if (mod < 92) return PostCategory.NEWS;
        return PostCategory.NOTICE;
    }

    /**
     * 게시글 상태 분포
     */
    private PostStatus selectStatus(int index) {
        int mod = index % 100;
        if (mod < 90) return PostStatus.PUBLISHED;
        if (mod < 96) return PostStatus.DRAFT;
        if (mod < 98) return PostStatus.HIDDEN;
        return PostStatus.DELETED;
    }

    /**
     * 메타데이터 생성 (작성일, 수정일 등)
     */
    private PostMetaData generateMetaData(int index, int totalCount) {
        double ratio = (double) index / totalCount;
        long daysAgo = (long) (365 * Math.pow(1 - ratio, 2));

        LocalDateTime createdAt = LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(index % 24)
                .minusMinutes(index % 60);

        LocalDateTime modifiedAt = null;
        LocalDateTime publishedAt = null;

        if (index % 10 < 3) {
            long modifiedDaysAgo = Math.max(0, daysAgo - random.nextInt((int) Math.max(1, daysAgo)));
            modifiedAt = LocalDateTime.now()
                    .minusDays(modifiedDaysAgo)
                    .minusHours(random.nextInt(24));
        }

        if (selectStatus(index) == PostStatus.PUBLISHED) {
            publishedAt = createdAt.plusMinutes(random.nextInt(60));
        }

        return new PostMetaData(createdAt, modifiedAt, publishedAt);
    }

    /**
     * 게시글 태그 생성 (영문)
     */
    private List<Tag> generateTags(int index) {
        List<Tag> tags = new ArrayList<>();

        if (index % 2 == 0) {
            String[] tagNames = {"Java", "Spring", "JPA", "Performance", "Optimization",
                    "Database", "Search", "Index", "Community", "Development"};

            int tagCount = 1 + (index % 3);
            for (int i = 0; i < tagCount; i++) {
                String tagName = tagNames[(index + i) % tagNames.length];
                tags.add(new Tag(tagName));
            }
        }
        return tags;
    }

    /**
     * 빠른 테스트용 (5만 건)
     */
    @Transactional
    public void generateQuickTestData() {
        generateTestData(50_000);
    }

    /**
     * 권장 테스트용 (10만 건)
     */
    @Transactional
    public void generateRecommendedTestData() {
        generateTestData(100_000);
    }

    /**
     * 대용량 테스트용 (50만 건)
     */
    @Transactional
    public void generateLargeTestData() {
        generateTestData(500_000);
    }

    private Post instantiatePost() {
        try {
            var constructor = Post.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Post 엔티티 인스턴스 생성에 실패했습니다", e);
        }
    }

    private void applyField(Post target, String fieldName, Object value) {
        try {
            var field = Post.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Post 필드 설정에 실패했습니다: " + fieldName, e);
        }
    }

    private void handleBatchProgress(int processedCount, int totalCount, long startTime) {
        if (processedCount == 0 || processedCount % BATCH_SIZE != 0) {
            return;
        }

        entityManager.flush();
        entityManager.clear();

        long elapsed = System.currentTimeMillis() - startTime;
        double rate = (double) processedCount / elapsed * 1000;
        log.info("📊 진행률: {}/{} ({:.1f}%) | {:.0f}건/초",
                processedCount, totalCount, (processedCount * 100.0 / totalCount), rate);
    }

    private void completeBatch(long startTime, int totalCount) {
        entityManager.flush();
        entityManager.clear();

        long total = System.currentTimeMillis() - startTime;
        log.info("✅ 테스트 데이터 생성 완료: {} 건 | 총 소요 시간: {} 초", totalCount, total / 1000.0);
    }
}
