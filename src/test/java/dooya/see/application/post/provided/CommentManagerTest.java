package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.application.post.required.PostRepository;
import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record CommentManagerTest(CommentManager commentManager, CommentRepository commentRepository, PostRepository postRepository, PostManager postManager, PostFinder postFinder, EntityManager entityManager) {
    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 1L;

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @DisplayName("")
        @Test
        void createComment() {
            createPost();
            CommentCreateRequest request = new CommentCreateRequest("테스트 게시물");
            Comment comment = commentManager.create(request, POST_ID, MEMBER_ID);

            assertThat(comment.getId()).isNotNull();
            assertThat(comment.getContent().text()).isEqualTo("테스트 게시물");
            assertThat(comment.getPostId()).isEqualTo(POST_ID);
            assertThat(comment.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(comment.getParentCommentId()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(comment.getMetaData().createdAt()).isNotNull();
            assertThat(comment.getMetaData().modifiedAt()).isNull();
        }

        @DisplayName("")
        @Test
        void a() {
            CommentCreateRequest request = new CommentCreateRequest("테스트 게시물");

            assertThatThrownBy(() -> commentManager.create(request, POST_ID, MEMBER_ID))
                .isInstanceOf(PostNotFoundException.class);
        }
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();

        return postFinder.find(post.getId());
    }
}