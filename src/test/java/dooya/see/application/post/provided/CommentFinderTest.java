package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.exception.CommentNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dooya.see.domain.post.PostFixture.createPostRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
public record CommentFinderTest(
        CommentFinder commentFinder,
        CommentManager commentManager,
        CommentRepository commentRepository,
        PostManager postManager,
        PostFinder postFinder,
        EntityManager entityManager) {
    @DisplayName("댓글 ID로 댓글을 조회한다")
    @Test
    void findCommentById() {
        Post post = createPost();
        Comment comment = createComment(post.getId(), 1L, "테스트 댓글");

        Comment foundComment = commentFinder.find(comment.getId());

        assertThat(foundComment.getId()).isEqualTo(comment.getId());
        assertThat(foundComment.getContent().text()).isEqualTo("테스트 댓글");
        assertThat(foundComment.getPostId()).isEqualTo(post.getId());
        assertThat(foundComment.getMemberId()).isEqualTo(1L);
        assertThat(foundComment.getParentCommentId()).isNull();
        assertThat(foundComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
        assertThat(foundComment.getMetaData().createdAt()).isNotNull();
        assertThat(foundComment.getMetaData().modifiedAt()).isNull();
    }

    @DisplayName("존재하지 않는 댓글 ID로 조회하면 예외가 발생한다")
    @Test
    void findCommentByNonexistentIdThrowsException() {
        Long nonexistentCommentId = 999L;

        assertThatThrownBy(() -> commentFinder.find(nonexistentCommentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("댓글을 찾을 수 없습니다: " + nonexistentCommentId);
    }

    @DisplayName("게시글 ID로 댓글 목록을 조회한다")
    @Test
    void findCommentsByPostId() {
        Post post = createPost();
        Comment comment1 = createComment(post.getId(), 1L, "첫 번째 댓글");
        Comment comment2 = createComment(post.getId(), 2L, "두 번째 댓글");
        Comment comment3 = createComment(post.getId(), 1L, "세 번째 댓글");

        List<Comment> comments = commentFinder.findByPostId(post.getId());

        assertThat(comments).hasSize(3);
        assertThat(comments)
                .extracting(Comment::getId)
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId(), comment3.getId());
    }

    @DisplayName("댓글이 없는 게시글의 댓글 목록 조회 시 빈 목록을 반환한다")
    @Test
    void findCommentsByPostIdWithNoCommentsReturnsEmptyList() {
        Post post = createPost();

        List<Comment> comments = commentFinder.findByPostId(post.getId());

        assertThat(comments).isEmpty();
    }

    @DisplayName("부모 댓글 ID로 답글 목록을 조회한다")
    @Test
    void findRepliesByParentCommentId() {
        Post post = createPost();
        Comment parentComment = createComment(post.getId(), 1L, "부모 댓글");
        Comment reply1 = createReply(post.getId(), 2L, "첫 번째 답글", parentComment.getId());
        Comment reply2 = createReply(post.getId(), 1L, "두 번째 답글", parentComment.getId());

        List<Comment> replies = commentFinder.findRepliesByParentId(parentComment.getId());

        assertThat(replies).hasSize(2);
        assertThat(replies)
                .extracting(Comment::getId)
                .containsExactlyInAnyOrder(reply1.getId(), reply2.getId());
        assertThat(replies)
                .extracting(Comment::getParentCommentId)
                .allMatch(parentId -> parentId.equals(parentComment.getId()));
    }

    @DisplayName("답글이 없는 댓글의 답글 목록 조회 시 빈 목록을 반환한다")
    @Test
    void findRepliesByParentCommentIdWithNoRepliesReturnsEmptyList() {
        Post post = createPost();
        Comment parentComment = createComment(post.getId(), 1L, "부모 댓글");

        List<Comment> replies = commentFinder.findRepliesByParentId(parentComment.getId());

        assertThat(replies).isEmpty();
    }

    @DisplayName("회원 ID로 해당 회원이 작성한 댓글 목록을 조회한다")
    @Test
    void findCommentsByMemberId() {
        Post post1 = createPost();
        Post post2 = createPost();
        
        Comment comment1 = createComment(post1.getId(), 1L, "첫 번째 댓글");
        Comment comment2 = createComment(post2.getId(), 1L, "두 번째 댓글");
        createComment(post1.getId(), 2L, "다른 회원 댓글");

        List<Comment> memberComments = commentFinder.findByMemberId(1L);

        assertThat(memberComments).hasSize(2);
        assertThat(memberComments)
                .extracting(Comment::getId)
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
        assertThat(memberComments)
                .extracting(Comment::getMemberId)
                .allMatch(memberId -> memberId.equals(1L));
    }

    @DisplayName("댓글을 작성하지 않은 회원의 댓글 목록 조회 시 빈 목록을 반환한다")
    @Test
    void findCommentsByMemberIdWithNoCommentsReturnsEmptyList() {
        Long memberWithNoComments = 999L;

        List<Comment> memberComments = commentFinder.findByMemberId(memberWithNoComments);

        assertThat(memberComments).isEmpty();
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();
        return postFinder.find(post.getId());
    }

    private Comment createComment(Long postId, Long memberId, String content) {
        Comment comment = commentManager.create(
                CommentFixture.createCommentRequest(content), 
                postId, 
                memberId
        );
        entityManager.flush();
        entityManager.clear();
        return commentFinder.find(comment.getId());
    }

    private Comment createReply(Long postId, Long memberId, String content, Long parentCommentId) {
        Comment reply = commentManager.create(
                CommentFixture.createReplyRequest(content, parentCommentId), 
                postId, 
                memberId
        );
        entityManager.flush();
        entityManager.clear();
        return commentFinder.find(reply.getId());
    }
}
