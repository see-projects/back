package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostNotFoundException;
import dooya.see.domain.post.PostStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        postManager.update(updateAllFieldsRequest(), post.getId());

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

        postManager.update(updateTitleOnlyRequest(), post.getId());

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

        postManager.update(updateBodyOnlyRequest(), post.getId());

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

        postManager.update(updateCategoryOnlyRequest(), post.getId());

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
    }

    @Test
    @DisplayName("변경사항이 없는 요청으로 수정 시 IllegalStateException이 발생한다")
    void updateWithNoChanges() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(noUpdateRequest(), post.getId()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하려고 하면 PostNotFoundException이 발생한다")
    void updateNonExistentPost() {
        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), 999L))
            .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("")
    void a() {
        Post post = createPost();

        postManager.publish(post.getId());

        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("")
    void b() {
        Post post = createPost();
        postManager.publish(post.getId());

        assertThatThrownBy(() -> postManager.publish(post.getId()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("")
    void c() {
        Post post = createPost();
        postManager.publish(post.getId());

        postManager.hide(post.getId());

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("")
    void d() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.hide(post.getId()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("")
    void e() {
        Post post = createPost();

        postManager.delete(post.getId());

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("")
    void f() {
        Post post = createPost();
        postManager.delete(post.getId());

        assertThatThrownBy(() -> postManager.delete(post.getId()))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("")
    void g() {
        Post post = createPost();
        postManager.publish(post.getId());

        postManager.incrementViewCount(post.getId());

        assertThat(post.getMetaData().viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("")
    void h() {
        Post post = createPost();

        postManager.incrementLikeCount(post.getId());
        
        assertThat(post.getMetaData().viewCount()).isZero();
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();

        return postFinder.find(post.getId());
    }
}
