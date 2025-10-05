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
 * 성능 테스트를 위한 게시글 테스트 데이터 생성기
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostTestDataGenerator {
    private final EntityManager entityManager;
    private final Random random = new Random();

    /**
     * 지정된 개수만큼 테스트 데이터를 생성합니다.
     * 권장: 10만 건 이상
     *
     * @param count 생성할 게시글 수
     */
    @Transactional
    public void generateTestData(int count) {
        log.info("=== 테스트 데이터 생성 시작: {} 건 ===", count);

        int batchSize = 1000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            Post post = createRealisticPost(i, count);
            entityManager.persist(post);

            // 배치 처리로 성능 최적화
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();

                long elapsed = System.currentTimeMillis() - startTime;
                double rate = (double) i / elapsed * 1000;
                log.info("진행률: {}/{} ({:.1f}%) | 속도: {:.0f} 건/초",
                        i, count, (i * 100.0 / count), rate);
            }
        }

        entityManager.flush();
        entityManager.clear();

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("=== 테스트 데이터 생성 완료: {} 건 | 소요 시간: {} 초 ===",
                count, totalTime / 1000.0);
    }

    /**
     * 실제 커뮤니티 특성을 반영한 게시글 생성
     */
    private Post createRealisticPost(int index, int totalCount) {
        Post post = new Post();

        // 리플렉션을 사용하여 private 필드 설정
        try {
            // content 설정
            var contentField = Post.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(post, new PostContent(
                    generateTitle(index),
                    generateBody(index)
            ));

            // memberId 설정 (파레토 법칙: 20%의 사용자가 80%의 글 작성)
            var memberIdField = Post.class.getDeclaredField("memberId");
            memberIdField.setAccessible(true);
            memberIdField.set(post, selectMemberId(index));

            // category 설정
            var categoryField = Post.class.getDeclaredField("category");
            categoryField.setAccessible(true);
            categoryField.set(post, selectCategory(index));

            // status 설정 (90% PUBLISHED, 8% DRAFT, 2% HIDDEN/DELETED)
            var statusField = Post.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(post, selectStatus(index));

            // metaData 설정
            var metaDataField = Post.class.getDeclaredField("metaData");
            metaDataField.setAccessible(true);
            metaDataField.set(post, generateMetaData(index, totalCount));

            // tags 설정
            var tagsField = Post.class.getDeclaredField("tags");
            tagsField.setAccessible(true);
            tagsField.set(post, generateTags(index));

        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 생성 실패", e);
        }

        return post;
    }

    private String generateTitle(int index) {
        String[] prefixes = {"공지", "질문", "정보", "후기", "토론", "개발", "뉴스"};
        String[] keywords = {"커뮤니티", "서비스", "기능", "업데이트", "버그",
                "개선", "제안", "문의", "공유", "테스트"};

        String prefix = prefixes[index % prefixes.length];
        String keyword = keywords[(index / prefixes.length) % keywords.length];

        return String.format("[%s] %s 관련 게시글 #%d", prefix, keyword, index);
    }

    private String generateBody(int index) {
        // 실제와 유사한 길이의 본문 (200~3000자)
        int targetLength = 200 + (index % 2800);
        StringBuilder body = new StringBuilder();

        String[] sentences = {
                "이것은 테스트 게시글의 본문 내용입니다. ",
                "커뮤니티 서비스의 검색 성능을 측정하기 위한 샘플 데이터입니다. ",
                "다양한 키워드와 내용을 포함하여 실제 환경을 시뮬레이션합니다. ",
                "게시글 검색 기능의 성능 개선을 위한 테스트를 진행하고 있습니다. ",
                "LIKE 쿼리의 성능 문제를 파악하고 최적화 방안을 찾고 있습니다. "
        };

        while (body.length() < targetLength) {
            body.append(sentences[random.nextInt(sentences.length)]);
        }

        // 검색 키워드 분포를 위해 특정 단어 삽입
        if (index % 10 == 0) body.append("중요한 공지사항입니다. ");
        if (index % 7 == 0) body.append("긴급 업데이트가 필요합니다. ");
        if (index % 5 == 0) body.append("자주 묻는 질문 FAQ. ");

        return body.substring(0, Math.min(targetLength, body.length()));
    }

    private Long selectMemberId(int index) {
        int totalUsers = 10_000;

        // 파레토 법칙: 20%의 사용자가 80%의 글 작성
        if (index % 5 < 4) {
            // 80%의 글은 20%의 활발한 사용자가 작성
            return (long) (1 + (index % (totalUsers / 5)));
        } else {
            // 20%의 글은 80%의 일반 사용자가 작성
            return (long) ((totalUsers / 5) + 1 + (index % (totalUsers * 4 / 5)));
        }
    }

    private PostCategory selectCategory(int index) {
        // 현실적인 분포
        int mod = index % 100;
        if (mod < 40) return PostCategory.GENERAL;  // 40%
        if (mod < 65) return PostCategory.TECH;     // 25%
        if (mod < 80) return PostCategory.QNA;      // 15%
        if (mod < 92) return PostCategory.NEWS;     // 12%
        return PostCategory.NOTICE;                  // 8%
    }

    private PostStatus selectStatus(int index) {
        int mod = index % 100;
        if (mod < 90) return PostStatus.PUBLISHED;  // 90%
        if (mod < 96) return PostStatus.DRAFT;      // 6%
        if (mod < 98) return PostStatus.HIDDEN;     // 2%
        return PostStatus.DELETED;                   // 2%
    }

    private PostMetaData generateMetaData(int index, int totalCount) {
        // 최근 1년간의 데이터, 최근일수록 더 많음 (지수 분포)
        double ratio = (double) index / totalCount;
        long daysAgo = (long) (365 * Math.pow(1 - ratio, 2)); // 최근에 더 밀집

        LocalDateTime createdAt = LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(index % 24)
                .minusMinutes(index % 60);

        LocalDateTime modifiedAt = null;
        LocalDateTime publishedAt = null;

        // 30%의 게시글이 수정됨
        if (index % 10 < 3) {
            long modifiedDaysAgo = daysAgo - (random.nextInt(Math.max(1, (int)daysAgo)));
            modifiedAt = LocalDateTime.now()
                    .minusDays(modifiedDaysAgo)
                    .minusHours(random.nextInt(24));
        }

        // PUBLISHED 상태면 publishedAt 설정
        if (selectStatus(index) == PostStatus.PUBLISHED) {
            publishedAt = createdAt.plusMinutes(random.nextInt(60));
        }

        return new PostMetaData(createdAt, modifiedAt, publishedAt);
    }

    private List<Tag> generateTags(int index) {
        List<Tag> tags = new ArrayList<>();

        // 50%의 게시글에 태그 추가
        if (index % 2 == 0) {
            String[] tagNames = {"Java", "Spring", "JPA", "성능", "최적화",
                    "데이터베이스", "검색", "인덱스", "커뮤니티", "개발"};

            // 1~3개의 태그 추가
            int tagCount = 1 + (index % 3);
            for (int i = 0; i < tagCount; i++) {
                String tagName = tagNames[(index + i) % tagNames.length];
                tags.add(new Tag(tagName));
            }
        }

        return tags;
    }

    /**
     * 빠른 테스트용 최소 데이터셋 (5만 건)
     */
    @Transactional
    public void generateQuickTestData() {
        generateTestData(50_000);
    }

    /**
     * 권장 테스트 데이터셋 (10만 건)
     */
    @Transactional
    public void generateRecommendedTestData() {
        generateTestData(100_000);
    }

    /**
     * 대용량 테스트 데이터셋 (50만 건)
     */
    @Transactional
    public void generateLargeTestData() {
        generateTestData(500_000);
    }
}