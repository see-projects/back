package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentFixture;
import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.exception.CommentNotFoundException;
import jakarta.persistence.EntityManager;
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
public record CommentFinderTest(CommentFinder commentFinder, CommentManager commentManager, CommentRepository commentRepository, PostManager postManager, PostFinder postFinder, EntityManager entityManager) {
    @Test
    void 댓글_ID로_댓글을_조회한다() {
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

    @Test
    void 존재하지_않는_댓글_ID로_조회하면_예외가_발생한다() {
        Long nonexistentCommentId = 999L;

        assertThatThrownBy(() -> commentFinder.find(nonexistentCommentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("댓글을 찾을 수 없습니다: " + nonexistentCommentId);
    }

    @Test
    void 게시글_ID로_댓글_목록을_조회한다() {
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

    @Test
    void 댓글이_없는_게시글의_댓글_목록_조회_시_빈_목록을_반환한다() {
        Post post = createPost();

        List<Comment> comments = commentFinder.findByPostId(post.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void 부모_댓글_ID로_답글_목록을_조회한다() {
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

    @Test
    void 답글이_없는_댓글의_답글_목록_조회_시_빈_목록을_반환한다() {
        Post post = createPost();
        Comment parentComment = createComment(post.getId(), 1L, "부모 댓글");

        List<Comment> replies = commentFinder.findRepliesByParentId(parentComment.getId());

        assertThat(replies).isEmpty();
    }

    @Test
    void 회원_ID로_해당_회원이_작성한_댓글_목록을_조회한다() {
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

    @Test
    void 댓글을_작성하지_않은_회원의_댓글_목록_조회_시_빈_목록을_반환한다() {
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
