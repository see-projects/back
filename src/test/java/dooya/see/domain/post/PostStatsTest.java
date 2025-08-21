package dooya.see.domain.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostStatsTest {
    
    PostStats postStats;
    
    @BeforeEach
    void setUp() {
        postStats = PostStats.create(1L);
    }

    @Test
    @DisplayName("PostStats 생성 시 postId가 설정되고 모든 통계 수치는 0으로 초기화된다")
    void createPostStats() {
        assertThat(postStats.getPostId()).isEqualTo(1L);
        assertThat(postStats.getViewCount()).isZero();
        assertThat(postStats.getLikeCount()).isZero();
        assertThat(postStats.getCommentCount()).isZero();
        assertThat(postStats.hasValidStats()).isTrue();
    }

    @Test
    @DisplayName("조회수를 증가시키면 viewCount가 1 증가한다")
    void incrementViewCount() {
        postStats.incrementViewCount();

        assertThat(postStats.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("조회수를 여러 번 증가시키면 증가한 횟수만큼 viewCount가 증가한다")
    void incrementViewCountMultipleTimes() {
        postStats.incrementViewCount();
        postStats.incrementViewCount();
        postStats.incrementViewCount();

        assertThat(postStats.getViewCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("좋아요 수를 증가시키면 likeCount가 1 증가한다")
    void incrementLikeCount() {
        postStats.incrementLikeCount();

        assertThat(postStats.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 수를 감소시키면 likeCount가 1 감소한다")
    void decrementLikeCount() {
        postStats.incrementLikeCount();
        postStats.incrementLikeCount();
        
        postStats.decrementLikeCount();

        assertThat(postStats.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 수가 0일 때 감소시켜도 likeCount는 0 이하로 내려가지 않는다")
    void decrementLikeCountWhenZero() {
        postStats.decrementLikeCount();

        assertThat(postStats.getLikeCount()).isZero();
    }

    @Test
    @DisplayName("댓글 수를 증가시키면 commentCount가 1 증가한다")
    void incrementCommentCount() {
        postStats.incrementCommentCount();

        assertThat(postStats.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 수를 감소시키면 commentCount가 1 감소한다")
    void decrementCommentCount() {
        postStats.incrementCommentCount();
        postStats.incrementCommentCount();
        
        postStats.decrementCommentCount();

        assertThat(postStats.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 수가 0일 때 감소시켜도 commentCount는 0 이하로 내려가지 않는다")
    void decrementCommentCountWhenZero() {
        postStats.decrementCommentCount();

        assertThat(postStats.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("조회수 1000 이상이거나 좋아요 100 이상이면 인기 게시물로 판단한다")
    void isPopular() {
        // 조회수 1000 이상
        for (int i = 0; i < 1000; i++) {
            postStats.incrementViewCount();
        }
        assertThat(postStats.isPopular()).isTrue();

        // 새 인스턴스로 좋아요 100 이상 테스트
        PostStats newStats = PostStats.create(2L);
        for (int i = 0; i < 100; i++) {
            newStats.incrementLikeCount();
        }
        assertThat(newStats.isPopular()).isTrue();

        // 조건 미만일 때
        PostStats unpopularStats = PostStats.create(3L);
        assertThat(unpopularStats.isPopular()).isFalse();
    }

    @Test
    @DisplayName("조회수 10000 이상이거나 좋아요 1000 이상이면 바이럴 게시물로 판단한다")
    void isViral() {
        // 조회수 10000 이상
        for (int i = 0; i < 10000; i++) {
            postStats.incrementViewCount();
        }
        assertThat(postStats.isViral()).isTrue();

        // 새 인스턴스로 좋아요 1000 이상 테스트
        PostStats newStats = PostStats.create(2L);
        for (int i = 0; i < 1000; i++) {
            newStats.incrementLikeCount();
        }
        assertThat(newStats.isViral()).isTrue();

        // 조건 미만일 때
        PostStats nonViralStats = PostStats.create(3L);
        assertThat(nonViralStats.isViral()).isFalse();
    }

    @Test
    @DisplayName("좋아요나 댓글이 하나라도 있으면 참여도가 있다고 판단한다")
    void hasEngagement() {
        assertThat(postStats.hasEngagement()).isFalse();

        postStats.incrementLikeCount();
        assertThat(postStats.hasEngagement()).isTrue();

        PostStats newStats = PostStats.create(2L);
        newStats.incrementCommentCount();
        assertThat(newStats.hasEngagement()).isTrue();
    }

    @Test
    @DisplayName("참여율은 (좋아요 + 댓글) / 조회수 * 100으로 계산된다")
    void getEngagementRate() {
        // 조회수가 0이면 참여율 0
        assertThat(postStats.getEngagementRate()).isZero();

        // 조회수 100, 좋아요 10, 댓글 5 = 참여율 15%
        for (int i = 0; i < 100; i++) {
            postStats.incrementViewCount();
        }
        for (int i = 0; i < 10; i++) {
            postStats.incrementLikeCount();
        }
        for (int i = 0; i < 5; i++) {
            postStats.incrementCommentCount();
        }

        assertThat(postStats.getEngagementRate()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("모든 통계 수치가 0 이상이면 유효한 통계로 판단한다")
    void hasValidStats() {
        assertThat(postStats.hasValidStats()).isTrue();

        postStats.incrementViewCount();
        postStats.incrementLikeCount();
        postStats.incrementCommentCount();
        
        assertThat(postStats.hasValidStats()).isTrue();
    }
}
