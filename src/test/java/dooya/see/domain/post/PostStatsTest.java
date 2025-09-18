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
    void PostStats_생성_시_postId가_설정되고_모든_통계_수치는_0으로_초기화된다() {
        assertThat(postStats.getPostId()).isEqualTo(1L);
        assertThat(postStats.getViewCount()).isZero();
        assertThat(postStats.getLikeCount()).isZero();
        assertThat(postStats.getCommentCount()).isZero();
        assertThat(postStats.hasValidStats()).isTrue();
    }

    @Test
    void 조회수를_증가시키면_viewCount가_1_증가한다() {
        postStats.incrementViewCount();

        assertThat(postStats.getViewCount()).isEqualTo(1);
    }

    @Test
    void 조회수를_여러_번_증가시키면_증가한_횟수만큼_viewCount가_증가한다() {
        postStats.incrementViewCount();
        postStats.incrementViewCount();
        postStats.incrementViewCount();

        assertThat(postStats.getViewCount()).isEqualTo(3);
    }

    @Test
    void 좋아요_수를_증가시키면_likeCount가_1_증가한다() {
        postStats.incrementLikeCount();

        assertThat(postStats.getLikeCount()).isEqualTo(1);
    }

    @Test
    void 좋아요_수를_감소시키면_likeCount가_1_감소한다() {
        postStats.incrementLikeCount();
        postStats.incrementLikeCount();
        
        postStats.decrementLikeCount();

        assertThat(postStats.getLikeCount()).isEqualTo(1);
    }

    @Test
    void 좋아요_수가_0일_때_감소시켜도_likeCount는_0_이하로_내려가지_않는다() {
        postStats.decrementLikeCount();

        assertThat(postStats.getLikeCount()).isZero();
    }

    @Test
    void 댓글_수를_증가시키면_commentCount가_1_증가한다() {
        postStats.incrementCommentCount();

        assertThat(postStats.getCommentCount()).isEqualTo(1);
    }

    @Test
    void 댓글_수를_감소시키면_commentCount가_1_감소한다() {
        postStats.incrementCommentCount();
        postStats.incrementCommentCount();
        
        postStats.decrementCommentCount();

        assertThat(postStats.getCommentCount()).isEqualTo(1);
    }

    @Test
    void 댓글_수가_0일_때_감소시켜도_commentCount는_0_이하로_내려가지_않는다() {
        postStats.decrementCommentCount();

        assertThat(postStats.getCommentCount()).isZero();
    }

    @Test
    void 조회수_1000_이상이거나_좋아요_100_이상이면_인기_게시물로_판단한다() {
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
    void 조회수_10000_이상이거나_좋아요_1000_이상이면_바이럴_게시물로_판단한다() {
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
    void 좋아요나_댓글이_하나라도_있으면_참여도가_있다고_판단한다() {
        assertThat(postStats.hasEngagement()).isFalse();

        postStats.incrementLikeCount();
        assertThat(postStats.hasEngagement()).isTrue();

        PostStats newStats = PostStats.create(2L);
        newStats.incrementCommentCount();
        assertThat(newStats.hasEngagement()).isTrue();
    }

    @Test
    void 참여율은_좋아요_댓글_조회수_100으로_계산된다() {
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
    void 모든_통계_수치가_0_이상이면_유효한_통계로_판단한다() {
        assertThat(postStats.hasValidStats()).isTrue();

        postStats.incrementViewCount();
        postStats.incrementLikeCount();
        postStats.incrementCommentCount();
        
        assertThat(postStats.hasValidStats()).isTrue();
    }
}
