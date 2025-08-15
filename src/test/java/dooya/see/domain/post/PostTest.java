package dooya.see.domain.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class PostTest {
    Post post;

    @BeforeEach
    void setUp() {
        post = Post.create(createPostRequest(), 1L);
    }

    @Test
    @DisplayName("Post 생성 시 요청 정보와 작성자 ID가 올바르게 설정되고 초기 상태는 DRAFT가 된다")
    void createPost() {
        assertThat(post.getContent().title()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getCategory()).isEqualTo(PostCategory.TECH);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData().createdAt()).isNotNull();
        assertThat(post.getMetaData().viewCount()).isZero();
        assertThat(post.getMetaData().likeCount()).isZero();
        assertThat(post.getMetaData().commentCount()).isZero();
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글을 발행하면 상태가 PUBLISHED로 변경되고 발행일시가 설정된다")
    void publishDraftPost() {
        post.publish();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }
}
