package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PostLikeTest {
    @Test
    @DisplayName("")
    void a() {
        PostLike postLike = PostLike.create(1L, 1L);

        assertNotNull(postLike);
        assertThat(postLike.getPostId()).isNotNull();
        assertThat(postLike.getMemberId()).isNotNull();
        assertThat(postLike.getLikedAt()).isNotNull();
    }
}