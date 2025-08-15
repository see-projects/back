package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMetaDataTest {
    
    @Test
    @DisplayName("PostMetaData 생성 시 생성일시가 설정되고 나머지 값들은 초기값으로 설정된다")
    void createPostMetaData() {
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData postMetaData = PostMetaData.create();
        
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
        
        assertThat(postMetaData.createdAt()).isBetween(beforeCreate, afterCreate);
        assertThat(postMetaData.modifiedAt()).isNull();
        assertThat(postMetaData.publishedAt()).isNull();
        assertThat(postMetaData.viewCount()).isZero();
        assertThat(postMetaData.likeCount()).isZero();
        assertThat(postMetaData.commentCount()).isZero();
    }
    
    @Test
    @DisplayName("수정일시 업데이트 시 modifiedAt만 현재 시간으로 변경되고 나머지는 기존 값을 유지한다")
    void updateModifiedAt() {
        PostMetaData original = PostMetaData.create();
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData updated = original.updateModifiedAt();
        
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertThat(updated.createdAt()).isEqualTo(original.createdAt());
        assertThat(updated.modifiedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(updated.publishedAt()).isEqualTo(original.publishedAt());
        assertThat(updated.viewCount()).isEqualTo(original.viewCount());
        assertThat(updated.likeCount()).isEqualTo(original.likeCount());
        assertThat(updated.commentCount()).isEqualTo(original.commentCount());
    }

    @Test
    @DisplayName("발행일시 업데이트 시 publishedAt만 현재 시간으로 변경되고 나머지는 기존 값을 유지한다")
    void updatePublishedAt() {
        PostMetaData original = PostMetaData.create();
        LocalDateTime beforePublish = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData published = original.updatePublishedAt();
        
        LocalDateTime afterPublish = LocalDateTime.now().plusSeconds(1);
        
        assertThat(published.createdAt()).isEqualTo(original.createdAt());
        assertThat(published.modifiedAt()).isEqualTo(original.modifiedAt());
        assertThat(published.publishedAt()).isBetween(beforePublish, afterPublish);
        assertThat(published.viewCount()).isEqualTo(original.viewCount());
        assertThat(published.likeCount()).isEqualTo(original.likeCount());
        assertThat(published.commentCount()).isEqualTo(original.commentCount());
    }

    @Test
    @DisplayName("조회수 증가 시 viewCount만 1 증가하고 나머지는 기존 값을 유지한다")
    void incrementViewCount() {
        PostMetaData original = PostMetaData.create();
        
        PostMetaData incremented = original.incrementViewCount();
        
        assertThat(incremented.createdAt()).isEqualTo(original.createdAt());
        assertThat(incremented.modifiedAt()).isEqualTo(original.modifiedAt());
        assertThat(incremented.publishedAt()).isEqualTo(original.publishedAt());
        assertThat(incremented.viewCount()).isEqualTo(original.viewCount() + 1);
        assertThat(incremented.likeCount()).isEqualTo(original.likeCount());
        assertThat(incremented.commentCount()).isEqualTo(original.commentCount());
    }

    @Test
    @DisplayName("좋아요 수 증가 시 likeCount만 1 증가하고 나머지는 기존 값을 유지한다")
    void incrementLikeCount() {
        PostMetaData original = PostMetaData.create();
        
        PostMetaData incremented = original.incrementLikeCount();
        
        assertThat(incremented.createdAt()).isEqualTo(original.createdAt());
        assertThat(incremented.modifiedAt()).isEqualTo(original.modifiedAt());
        assertThat(incremented.publishedAt()).isEqualTo(original.publishedAt());
        assertThat(incremented.viewCount()).isEqualTo(original.viewCount());
        assertThat(incremented.likeCount()).isEqualTo(original.likeCount() + 1);
        assertThat(incremented.commentCount()).isEqualTo(original.commentCount());
    }

    @Test
    @DisplayName("댓글 수 증가 시 commentCount만 1 증가하고 나머지는 기존 값을 유지한다")
    void incrementCommentCount() {
        PostMetaData original = PostMetaData.create();
        
        PostMetaData incremented = original.incrementCommentCount();
        
        assertThat(incremented.createdAt()).isEqualTo(original.createdAt());
        assertThat(incremented.modifiedAt()).isEqualTo(original.modifiedAt());
        assertThat(incremented.publishedAt()).isEqualTo(original.publishedAt());
        assertThat(incremented.viewCount()).isEqualTo(original.viewCount());
        assertThat(incremented.likeCount()).isEqualTo(original.likeCount());
        assertThat(incremented.commentCount()).isEqualTo(original.commentCount() + 1);
    }

    @Test
    @DisplayName("여러 번 증가 메서드 호출 시 누적적으로 증가한다")
    void multipleIncrements() {
        PostMetaData original = PostMetaData.create();
        
        PostMetaData result = original
                .incrementViewCount()
                .incrementViewCount()
                .incrementLikeCount()
                .incrementCommentCount();
        
        assertThat(result.viewCount()).isEqualTo(2);
        assertThat(result.likeCount()).isEqualTo(1);
        assertThat(result.commentCount()).isEqualTo(1);
    }
}
