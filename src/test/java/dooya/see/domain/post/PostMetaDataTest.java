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
    }
}
