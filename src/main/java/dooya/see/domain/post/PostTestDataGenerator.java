package dooya.see.domain.post;

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
 * 대용량 성능 테스트용 게시글 데이터 생성기 (최적화 버전)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostTestDataGenerator {

    private final EntityManager entityManager;
    private final Random random = new Random();

    /**
     * 100만 건 대용량 테스트 데이터 생성
     */
    @Transactional
    public void generateMillionTestData() {
        generateTestData(1_000_000, 10_000);
    }

    /**
     * 지정된 개수만큼 테스트 데이터를 생성
     * @param count 생성할 게시글 수
     * @param batchSize flush/clear 단위
     */
    @Transactional
    public void generateTestData(int count, int batchSize) {
        log.info("🚀 대용량 테스트 데이터 생성 시작: {}건", count);

        long startTime = System.currentTimeMillis();
        List<Post> buffer = new ArrayList<>(batchSize);

        for (int i = 1; i <= count; i++) {
            buffer.add(createPost(i, count));

            if (buffer.size() >= batchSize) {
                persistBatch(buffer);
                buffer.clear();
                logProgress(i, count, startTime);
            }
        }

        // 남은 데이터 처리
        if (!buffer.isEmpty()) persistBatch(buffer);

        long total = System.currentTimeMillis() - startTime;
        log.info("✅ 생성 완료: {}건 | 총 소요: {}초", count, total / 1000.0);
    }

    /**
     * 배치 단위 저장 및 flush/clear
     */
    private void persistBatch(List<Post> posts) {
        for (Post post : posts) {
            entityManager.persist(post);
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * 진행률 로그 (100,000건마다)
     */
    private void logProgress(int current, int total, long startTime) {
        if (current % 100_000 == 0) {
            double elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0;
            double rate = current / elapsedSec;
            log.info("📊 진행률: {}/{} ({:.1f}%) | {:.0f}건/초", current, total, (current * 100.0 / total), rate);
        }
    }

    /**
     * realistic한 게시글 1개 생성
     */
    private Post createPost(int index, int totalCount) {
        Post post = new Post();

        PostContent content = new PostContent(generateTitle(index), generateBody(index));
        Long memberId = selectMemberId(index);
        PostCategory category = selectCategory(index);
        PostStatus status = selectStatus(index);
        PostMetaData metaData = generateMetaData(index, totalCount);
        List<Tag> tags = generateTags(index);

        setField(post, "content", content);
        setField(post, "memberId", memberId);
        setField(post, "category", category);
        setField(post, "status", status);
        setField(post, "metaData", metaData);
        setField(post, "tags", tags);

        return post;
    }

    /**
     * Reflection으로 private 필드 주입
     */
    private void setField(Post post, String fieldName, Object value) {
        try {
            var field = Post.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(post, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }

    // ────────────────────────────
    // 데이터 생성 로직
    // ────────────────────────────

    private String generateTitle(int index) {
        String[] prefixes = {"공지", "질문", "정보", "후기", "토론", "개발", "뉴스"};
        String[] keywords = {"커뮤니티", "서비스", "기능", "업데이트", "버그", "개선", "제안", "문의", "공유", "테스트"};

        String prefix = prefixes[index % prefixes.length];
        String keyword = keywords[(index / prefixes.length) % keywords.length];
        return String.format("[%s] %s 관련 게시글 #%d", prefix, keyword, index);
    }

    private String generateBody(int index) {
        int targetLength = 200 + (index % 2800);
        StringBuilder body = new StringBuilder();

        String[] sentences = {
                "이것은 테스트 게시글의 본문 내용입니다. ",
                "검색 성능 측정을 위한 데이터입니다. ",
                "다양한 키워드와 내용을 포함합니다. ",
                "게시글 검색 기능의 성능 개선을 위한 테스트입니다. ",
                "LIKE 쿼리와 FULLTEXT 성능 비교를 수행합니다. "
        };

        while (body.length() < targetLength) {
            body.append(sentences[random.nextInt(sentences.length)]);
        }

        // 일부 게시글에 검색 키워드 삽입
        if (index % 10 == 0) body.append("중요한 공지사항입니다. ");
        if (index % 7 == 0) body.append("긴급 업데이트가 필요합니다. ");
        if (index % 5 == 0) body.append("자주 묻는 질문 FAQ. ");

        return body.substring(0, Math.min(targetLength, body.length()));
    }

    private Long selectMemberId(int index) {
        int totalUsers = 10_000;
        if (index % 5 < 4) {
            return (long) (1 + (index % (totalUsers / 5)));
        } else {
            return (long) ((totalUsers / 5) + 1 + (index % (totalUsers * 4 / 5)));
        }
    }

    private PostCategory selectCategory(int index) {
        int mod = index % 100;
        if (mod < 40) return PostCategory.GENERAL;
        if (mod < 65) return PostCategory.TECH;
        if (mod < 80) return PostCategory.QNA;
        if (mod < 92) return PostCategory.NEWS;
        return PostCategory.NOTICE;
    }

    private PostStatus selectStatus(int index) {
        int mod = index % 100;
        if (mod < 90) return PostStatus.PUBLISHED;
        if (mod < 96) return PostStatus.DRAFT;
        if (mod < 98) return PostStatus.HIDDEN;
        return PostStatus.DELETED;
    }

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

    private List<Tag> generateTags(int index) {
        List<Tag> tags = new ArrayList<>();
        if (index % 2 == 0) {
            String[] tagNames = {"Java", "Spring", "JPA", "성능", "최적화",
                    "데이터베이스", "검색", "인덱스", "커뮤니티", "개발"};
            int tagCount = 1 + (index % 3);
            for (int i = 0; i < tagCount; i++) {
                String tagName = tagNames[(index + i) % tagNames.length];
                tags.add(new Tag(tagName));
            }
        }
        return tags;
    }
}