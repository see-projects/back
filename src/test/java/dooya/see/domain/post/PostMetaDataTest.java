package dooya.see.domain.post;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMetaDataTest {
    @Test
    void PostMetaData_생성_시_생성일시가_설정되고_나머지_값들은_초기값으로_설정된다() {
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData postMetaData = PostMetaData.create();
        
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);
        
        assertThat(postMetaData.createdAt()).isBetween(beforeCreate, afterCreate);
        assertThat(postMetaData.modifiedAt()).isNull();
        assertThat(postMetaData.publishedAt()).isNull();
    }

    @Test
    void 수정일시_업데이트_시_modifiedAt만_현재_시간으로_변경되고_나머지는_기존_값을_유지한다() {
        PostMetaData original = PostMetaData.create();
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData updated = original.updateModifiedAt();
        
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertThat(updated.createdAt()).isEqualTo(original.createdAt());
        assertThat(updated.modifiedAt()).isBetween(beforeUpdate, afterUpdate);
        assertThat(updated.publishedAt()).isEqualTo(original.publishedAt());
    }

    @Test
    void 발행일시_업데이트_시_publishedAt만_현재_시간으로_변경되고_나머지는_기존_값을_유지한다() {
        PostMetaData original = PostMetaData.create();
        LocalDateTime beforePublish = LocalDateTime.now().minusSeconds(1);
        
        PostMetaData published = original.updatePublishedAt();
        
        LocalDateTime afterPublish = LocalDateTime.now().plusSeconds(1);
        
        assertThat(published.createdAt()).isEqualTo(original.createdAt());
        assertThat(published.modifiedAt()).isEqualTo(original.modifiedAt());
        assertThat(published.publishedAt()).isBetween(beforePublish, afterPublish);
    }
}
