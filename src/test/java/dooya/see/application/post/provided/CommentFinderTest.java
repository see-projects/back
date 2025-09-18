package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentFixture;
import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.exception.CommentNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
record CommentFinderTest(CommentFinder commentFinder, CommentManager commentManager, CommentRepository commentRepository, PostManager postManager, PostFinder postFinder, EntityManager entityManager) {
    private static final Long AUTHOR_ID = 1L;
    private static final Long ANOTHER_MEMBER_ID = 2L;
    private static final String COMMENT_CONTENT = "테스트 댓글";

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 댓글_단건_조회 {
        @Test
        void ID로_댓글을_조회한다() {
            Post post = createTestPost();
            Comment comment = createTestComment(post.getId(), AUTHOR_ID, COMMENT_CONTENT);

            Comment foundComment = commentFinder.find(comment.getId());

            assertThatCommentFound(foundComment, comment, post);
        }

        @Test
        void 존재하지_않는_ID로_조회_시_예외가_발생한다() {
            Long nonexistentCommentId = 999L;

            assertThatThrownBy(() -> commentFinder.find(nonexistentCommentId))
                    .isInstanceOf(CommentNotFoundException.class)
                    .hasMessage("댓글을 찾을 수 없습니다: " + nonexistentCommentId);
        }

        private void assertThatCommentFound(Comment foundComment, Comment expectedComment, Post post) {
            assertThat(foundComment.getId()).isEqualTo(expectedComment.getId());
            assertThat(foundComment.getContent().text()).isEqualTo(COMMENT_CONTENT);
            assertThat(foundComment.getPostId()).isEqualTo(post.getId());
            assertThat(foundComment.getMemberId()).isEqualTo(AUTHOR_ID);
            assertThat(foundComment.getParentCommentId()).isNull();
            assertThat(foundComment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            assertThat(foundComment.getMetaData().createdAt()).isNotNull();
            assertThat(foundComment.getMetaData().modifiedAt()).isNull();
        }
    }

    @Nested
    class 게시글별_댓글_조회 {
        @Test
        void 게시글_ID로_댓글_목록을_조회한다() {
            Post post = createTestPost();
            Comment comment1 = createTestComment(post.getId(), AUTHOR_ID, "첫 번째 댓글");
            Comment comment2 = createTestComment(post.getId(), ANOTHER_MEMBER_ID, "두 번째 댓글");
            Comment comment3 = createTestComment(post.getId(), AUTHOR_ID, "세 번째 댓글");

            List<Comment> comments = commentFinder.findByPostId(post.getId());

            assertThatCommentsFound(comments, comment1, comment2, comment3);
        }

        @Test
        void 댓글이_없는_게시글의_댓글_목록_조회_시_빈_목록을_반환한다() {
            Post post = createTestPost();

            List<Comment> comments = commentFinder.findByPostId(post.getId());

            assertThat(comments).isEmpty();
        }

        private void assertThatCommentsFound(List<Comment> comments, Comment... expectedComments) {
            assertThat(comments).hasSize(expectedComments.length);
            assertThat(comments)
                    .extracting(Comment::getId)
                    .containsExactlyInAnyOrder(
                            extractIds(expectedComments)
                    );
        }

        private Long[] extractIds(Comment... comments) {
            return java.util.Arrays.stream(comments)
                    .map(Comment::getId)
                    .toArray(Long[]::new);
        }
    }

    @Nested
    class 답글_조회 {
        @Test
        void 부모_댓글_ID로_답글_목록을_조회한다() {
            Post post = createTestPost();
            Comment parentComment = createTestComment(post.getId(), AUTHOR_ID, "부모 댓글");
            Comment reply1 = createTestReply(post.getId(), ANOTHER_MEMBER_ID, "첫 번째 답글", parentComment.getId());
            Comment reply2 = createTestReply(post.getId(), AUTHOR_ID, "두 번째 답글", parentComment.getId());

            List<Comment> replies = commentFinder.findRepliesByParentId(parentComment.getId());

            assertThatRepliesFound(replies, parentComment.getId(), reply1, reply2);
        }

        @Test
        void 답글이_없는_댓글의_답글_목록_조회_시_빈_목록을_반환한다() {
            Post post = createTestPost();
            Comment parentComment = createTestComment(post.getId(), AUTHOR_ID, "부모 댓글");

            List<Comment> replies = commentFinder.findRepliesByParentId(parentComment.getId());

            assertThat(replies).isEmpty();
        }

        private void assertThatRepliesFound(List<Comment> replies, Long expectedParentId, Comment... expectedReplies) {
            assertThat(replies).hasSize(expectedReplies.length);
            assertThat(replies)
                    .extracting(Comment::getId)
                    .containsExactlyInAnyOrder(
                            extractIds(expectedReplies)
                    );
            assertThat(replies)
                    .extracting(Comment::getParentCommentId)
                    .allMatch(parentId -> parentId.equals(expectedParentId));
        }

        private Long[] extractIds(Comment... comments) {
            return java.util.Arrays.stream(comments)
                    .map(Comment::getId)
                    .toArray(Long[]::new);
        }
    }

    @Nested
    class 회원별_댓글_조회 {
        @Test
        void 회원_ID로_해당_회원이_작성한_댓글_목록을_조회한다() {
            Post post1 = createTestPost();
            Post post2 = createTestPost();

            Comment comment1 = createTestComment(post1.getId(), AUTHOR_ID, "첫 번째 댓글");
            Comment comment2 = createTestComment(post2.getId(), AUTHOR_ID, "두 번째 댓글");
            createTestComment(post1.getId(), ANOTHER_MEMBER_ID, "다른 회원 댓글");

            List<Comment> memberComments = commentFinder.findByMemberId(AUTHOR_ID);

            assertThatMemberCommentsFound(memberComments, AUTHOR_ID, comment1, comment2);
        }

        @Test
        void 댓글을_작성하지_않은_회원의_댓글_목록_조회_시_빈_목록을_반환한다() {
            Long memberWithNoComments = 999L;

            List<Comment> memberComments = commentFinder.findByMemberId(memberWithNoComments);

            assertThat(memberComments).isEmpty();
        }

        private void assertThatMemberCommentsFound(List<Comment> comments, Long expectedMemberId, Comment... expectedComments) {
            assertThat(comments).hasSize(expectedComments.length);
            assertThat(comments)
                    .extracting(Comment::getId)
                    .containsExactlyInAnyOrder(
                            extractIds(expectedComments)
                    );
            assertThat(comments)
                    .extracting(Comment::getMemberId)
                    .allMatch(memberId -> memberId.equals(expectedMemberId));
        }

        private Long[] extractIds(Comment... comments) {
            return java.util.Arrays.stream(comments)
                    .map(Comment::getId)
                    .toArray(Long[]::new);
        }
    }

    // 헬퍼 메서드들
    private Post createTestPost() {
        Post post = postManager.create(createPostRequest(), AUTHOR_ID);
        flushAndClearContext();
        return postFinder.find(post.getId());
    }

    private Comment createTestComment(Long postId, Long memberId, String content) {
        Comment comment = commentManager.create(
                CommentFixture.createCommentRequest(content),
                postId,
                memberId
        );
        flushAndClearContext();
        return commentFinder.find(comment.getId());
    }

    private Comment createTestReply(Long postId, Long memberId, String content, Long parentCommentId) {
        Comment reply = commentManager.create(
                CommentFixture.createReplyRequest(content, parentCommentId),
                postId,
                memberId
        );
        flushAndClearContext();
        return commentFinder.find(reply.getId());
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}