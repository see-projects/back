package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostManagerTest(PostManager postManager, EntityManager entityManager, PostFinder postFinder) {

    @Test
    @DisplayName("게시글 생성 시 ID가 할당되고 초기 상태는 DRAFT가 된다")
    void create() {
        Post post = createPost();

        assertThat(post.getId()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData()).isNotNull();
    }

    @Test
    @DisplayName("모든 필드를 수정하면 제목, 내용, 카테고리가 모두 변경된다")
    void updateAllFields() {
        Post post = createPost();

        postManager.update(updateAllFieldsRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("수정된 제목");
        assertThat(post.getContent().body()).isEqualTo("수정된 내용");
        assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
    }

    @Test
    @DisplayName("제목만 수정하면 제목만 변경되고 내용과 카테고리는 기존 값을 유지한다")
    void updateTitleOnly() {
        Post post = createPost();

        String originalBody = post.getContent().body();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateTitleOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("새로운 제목");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    @DisplayName("내용만 수정하면 내용만 변경되고 제목과 카테고리는 기존 값을 유지한다")
    void updateBodyOnly() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateBodyOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo("새로운 내용");
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    @DisplayName("카테고리만 수정하면 카테고리만 변경되고 제목과 내용은 기존 값을 유지한다")
    void updateCategoryOnly() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        String originalBody = post.getContent().body();

        postManager.update(updateCategoryOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
    }

    @Test
    @DisplayName("변경사항이 없는 요청으로 수정 시 IllegalStateException이 발생한다")
    void updateWithNoChanges() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(noUpdateRequest(), post.getId(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하려고 하면 PostNotFoundException이 발생한다")
    void updateNonExistentPost() {
        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), 999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 수정 시도 시 UnauthorizedPostAccessException이 발생한다")
    void updateByNonOwner() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), post.getId(), 2L))
                .isInstanceOf(UnauthorizedPostAccessException.class);
    }

    @Test
    @DisplayName("게시글을 발행하면 PUBLISHED 상태가 되고 발행 시간이 기록된다")
    void publishDraftPost() {
        Post post = createPost();

        postManager.publish(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 발행된 게시글을 다시 발행하려고 하면 IllegalStateException이 발생한다")
    void publishAlreadyPublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.publish(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("발행된 게시글을 숨김 처리하면 HIDDEN 상태로 변경된다")
    void hidePublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.hide(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글을 숨김 처리하려고 하면 IllegalStateException이 발생한다")
    void hideDraftPost() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.hide(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("게시글을 삭제하면 DELETED 상태로 변경된다")
    void deletePost() {
        Post post = createPost();

        postManager.delete(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 다시 삭제하려고 하면 IllegalStateException이 발생한다")
    void deleteAlreadyDeletedPost() {
        Post post = createPost();
        postManager.delete(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.delete(post.getId(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("발행된 게시글의 조회수를 증가시키면 viewCount가 1 증가한다")
    void incrementViewCountOnPublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.incrementViewCount(post.getId());

        assertThat(post.getMetaData().viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글에 조회수 증가를 시도하면 viewCount는 변경되지 않는다")
    void incrementViewCountOnDraftPost() {
        Post post = createPost();

        postManager.incrementViewCount(post.getId());

        assertThat(post.getMetaData().viewCount()).isZero();
    }

    @Test
    @DisplayName("발행된 게시글의 좋아요 수를 증가시키면 likeCount가 1 증가한다")
    void incrementLikeCountOnPublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.incrementLikeCount(post.getId());

        assertThat(post.getMetaData().likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글에 좋아요 수 증가를 시도하면 likeCount는 변경되지 않는다")
    void incrementLikeCountOnDraftPost() {
        Post post = createPost();

        postManager.incrementLikeCount(post.getId());

        assertThat(post.getMetaData().likeCount()).isZero();
    }

    @Test
    @DisplayName("발행된 게시글의 댓글 수를 증가시키면 commentCount가 1 증가한다")
    void incrementCommentCountOnPublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.incrementCommentCount(post.getId());

        assertThat(post.getMetaData().commentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글에 댓글 수 증가를 시도하면 commentCount는 변경되지 않는다")
    void incrementCommentCountOnDraftPost() {
        Post post = createPost();

        postManager.incrementCommentCount(post.getId());

        assertThat(post.getMetaData().commentCount()).isZero();
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();

        return postFinder.find(post.getId());
    }
}
