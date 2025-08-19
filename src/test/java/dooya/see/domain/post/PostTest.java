package dooya.see.domain.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("모든 필드를 업데이트하면 제목, 내용, 카테고리가 모두 변경되고 수정일시가 설정된다")
    void updateAllFields() {
        PostUpdateRequest request = updateAllFieldsRequest();

        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("수정된 제목");
        assertThat(post.getContent().body()).isEqualTo("수정된 내용");
        assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("제목만 업데이트하면 제목만 변경되고 내용과 카테고리는 기존 값을 유지한다")
    void updateTitleOnly() {
        String originalBody = post.getContent().body();
        PostCategory originalCategory = post.getCategory();

        PostUpdateRequest request = updateTitleOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("새로운 제목");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(originalCategory);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("내용만 업데이트하면 내용만 변경되고 제목과 카테고리는 기존 값을 유지한다")
    void updateBodyOnly() {
        String originalTitle = post.getContent().title();
        PostCategory originalCategory = post.getCategory();

        PostUpdateRequest request = updateBodyOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo("새로운 내용");
        assertThat(post.getCategory()).isEqualTo(originalCategory);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("카테고리만 업데이트하면 카테고리만 변경되고 제목과 내용은 기존 값을 유지한다")
    void updateCategoryOnly() {
        String originalTitle = post.getContent().title();
        String originalBody = post.getContent().body();

        PostUpdateRequest request = updateCategoryOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("제목과 카테고리를 함께 업데이트하면 해당 필드들만 변경되고 내용은 기존 값을 유지한다")
    void updateTitleAndCategory() {
        String originalBody = post.getContent().body();

        PostUpdateRequest request = updateTitleAndCategoryRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("제목과 카테고리 변경");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.GENERAL);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("변경사항이 없는 요청으로 업데이트 시 IllegalStateException이 발생한다")
    void updateWithNoChanges() {
        PostUpdateRequest request = noUpdateRequest();

        assertThatThrownBy(() -> post.update(request))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글을 발행하면 상태가 PUBLISHED로 변경되고 발행일시가 설정된다")
    void publishDraftPost() {
        post.publish();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글을 숨김 처리하면 상태가 HIDDEN으로 변경된다")
    void hidePublishedPost() {
        post.publish();
        post.hide();

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("HIDDEN 상태의 게시글을 발행하면 상태가 PUBLISHED로 변경되고 발행일시가 갱신된다")
    void publishHiddenPost() {
        post.publish();
        post.hide();
        
        post.publish();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글을 숨김 처리하면 상태가 HIDDEN으로 변경된다")
    void hideDraftPost() {
        post.hide();

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글을 다시 발행하려고 하면 InvalidPostStatusTransitionException이 발생한다")
    void publishAlreadyPublishedPostThrowsException() {
        post.publish();
        
        assertThatThrownBy(() -> post.publish())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("HIDDEN 상태의 게시글을 다시 숨기려고 하면 InvalidPostStatusTransitionException이 발생한다")
    void hideAlreadyHiddenPostThrowsException() {
        post.publish();
        post.hide();
        
        assertThatThrownBy(() -> post.hide())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("게시글을 삭제하면 상태가 DELETED로 변경된다")
    void deletePost() {
        post.delete();

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 다시 삭제하려고 하면 InvalidPostStatusTransitionException이 발생한다")
    void deleteAlreadyDeletedPostThrowsException() {
        post.delete();

        assertThatThrownBy(() -> post.delete())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("게시글 작성자 확인 시 올바른 memberId면 true, 다른 memberId면 false를 반환한다")
    void isWrittenByMember() {
        assertThat(post.isWrittenBy(1L)).isTrue();
        assertThat(post.isWrittenBy(2L)).isFalse();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글 조회수를 증가시키면 viewCount가 1 증가한다")
    void incrementViewCountForPublishedPost() {
        post.publish();
        post.incrementViewCount();

        assertThat(post.getMetaData().viewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DELETED 상태의 게시글 조회수를 증가시켜도 viewCount는 변경되지 않는다")
    void incrementViewCountForDeletedPost() {
        post.delete();
        post.incrementViewCount();

        assertThat(post.getMetaData().viewCount()).isZero();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글 좋아요 수를 증가시키면 likeCount가 1 증가한다")
    void incrementLikeCountForPublishedPost() {
        post.publish();
        post.incrementLikeCount();

        assertThat(post.getMetaData().likeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DELETED 상태의 게시글 좋아요 수를 증가시켜도 likeCount는 변경되지 않는다")
    void incrementLikeCountForDeletedPost() {
        post.delete();
        post.incrementLikeCount();

        assertThat(post.getMetaData().likeCount()).isZero();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글 댓글 수를 증가시키면 commentCount가 1 증가한다")
    void incrementCommentCountForPublishedPost() {
        post.publish();
        post.incrementCommentCount();

        assertThat(post.getMetaData().commentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("DELETED 상태의 게시글 댓글 수를 증가시켜도 commentCount는 변경되지 않는다")
    void incrementCommentCountForDeletedPost() {
        post.delete();
        post.incrementCommentCount();

        assertThat(post.getMetaData().commentCount()).isZero();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글이 키워드 검색에 매칭되면 true를 반환한다")
    void a() {
        post.publish();

        PostSearchRequest request = PostFixture.searchPostRequest();

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글은 키워드 검색에 매칭되지 않아 false를 반환한다")
    void b() {
        PostSearchRequest request = PostFixture.searchPostRequest();

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("HIDDEN 상태의 게시글은 키워드 검색에 매칭되지 않아 false를 반환한다")
    void c() {
        post.hide();

        PostSearchRequest request = PostFixture.searchPostRequest();

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글 제목에 키워드가 포함되면 검색에 매칭되어 true를 반환한다")
    void d() {
        Post post = createPublishedPost("Spring Boot Tutorial", "자바 웹 개발 강의");

        PostSearchRequest request = new PostSearchRequest(
                "spring", null, null, null, null, null, null, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("PUBLISHED 상태의 게시글 내용에 키워드가 포함되면 검색에 매칭되어 true를 반환한다")
    void e() {
        Post post = createPublishedPost("웹 개발 강의", "Spring Boot를 사용한 REST API 개발");
        PostSearchRequest request = new PostSearchRequest(
                "spring", null, null, null, null, null, null, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("게시글의 카테고리가 검색 조건과 일치하면 true, 일치하지 않으면 false를 반환한다")
    void f() {
        Post techPost = createPublishedPostWithCategory("자바 강의", "객체지향 프로그래밍", PostCategory.TECH);
        Post noticePost = createPublishedPostWithCategory("공지 입니다", "공지 내용", PostCategory.NOTICE);

        PostSearchRequest request = new PostSearchRequest(
                null, null, null, PostCategory.TECH, null, null, null, null);

        assertThat(techPost.matchesSearchRequest(request)).isTrue();
        assertThat(noticePost.matchesSearchRequest(request)).isFalse();
    }

    @Test
    @DisplayName("복합 검색 조건에서 키워드와 카테고리가 모두 매칭되면 true, 하나라도 매칭되지 않으면 false를 반환한다")
    void g() {
        Post techPost = createPublishedPostWithCategory("스프링 강의", "스프링 부트 개발", PostCategory.TECH);
        Post noticePost = createPublishedPostWithCategory("스프링 강의 공지", "공지 내용", PostCategory.NOTICE);

        PostSearchRequest request = new PostSearchRequest(
                "스프링", null, null, PostCategory.TECH, null, null, null, null);

        assertThat(techPost.matchesSearchRequest(request)).isTrue();
        assertThat(noticePost.matchesSearchRequest(request)).isFalse();
    }

    @Test
    @DisplayName("DELETED 상태의 게시글은 키워드 검색에 매칭되지 않아 false를 반환한다")
    void deletedPostDoesNotMatchKeywordSearch() {
        post.publish();
        post.delete();
        PostSearchRequest request = new PostSearchRequest(
                "테스트", null, null, null, null, null, null, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("제목 키워드 검색 조건에 매칭되면 true, 매칭되지 않으면 false를 반환한다")
    void titleKeywordSearchMatching() {
        post.publish();
        
        PostSearchRequest matchingRequest = new PostSearchRequest(
                null, "테스트", null, null, null, null, null, null);
        PostSearchRequest nonMatchingRequest = new PostSearchRequest(
                null, "파이썬", null, null, null, null, null, null);

        assertThat(post.matchesSearchRequest(matchingRequest)).isTrue();
        assertThat(post.matchesSearchRequest(nonMatchingRequest)).isFalse();
    }

    @Test
    @DisplayName("내용 키워드 검색 조건에 매칭되면 true, 매칭되지 않으면 false를 반환한다")
    void contentKeywordSearchMatching() {
        post.publish();
        
        PostSearchRequest matchingRequest = new PostSearchRequest(
                null, null, "테스트", null, null, null, null, null);
        PostSearchRequest nonMatchingRequest = new PostSearchRequest(
                null, null, "파이썬", null, null, null, null, null);

        assertThat(post.matchesSearchRequest(matchingRequest)).isTrue();
        assertThat(post.matchesSearchRequest(nonMatchingRequest)).isFalse();
    }

    @Test
    @DisplayName("작성자 ID가 검색 조건과 일치하면 true, 일치하지 않으면 false를 반환한다")
    void memberIdSearchMatching() {
        post.publish();
        
        PostSearchRequest matchingRequest = new PostSearchRequest(
                null, null, null, null, 1L, null, null, null);
        PostSearchRequest nonMatchingRequest = new PostSearchRequest(
                null, null, null, null, 2L, null, null, null);

        assertThat(post.matchesSearchRequest(matchingRequest)).isTrue();
        assertThat(post.matchesSearchRequest(nonMatchingRequest)).isFalse();
    }

    @Test
    @DisplayName("모든 검색 조건이 null이면 발행된 게시글은 매칭되어 true를 반환한다")
    void publishedPostMatchesWhenAllSearchConditionsAreNull() {
        post.publish();
        PostSearchRequest request = new PostSearchRequest(
                null, null, null, null, null, null, null, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("키워드가 제목과 내용 모두에 포함되지 않으면 false를 반환한다")
    void keywordNotFoundInTitleAndContent() {
        post.publish();
        PostSearchRequest request = new PostSearchRequest(
                "파이썬", null, null, null, null, null, null, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("생성일시가 검색 시작 날짜 이후이면 날짜 범위 조건에 매칭되어 true를 반환한다")
    void createdDateAfterFromDateMatches() {
        post.publish();
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        PostSearchRequest request = new PostSearchRequest(
                null, null, null, null, null, null, fromDate, null);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("생성일시가 검색 종료 날짜 이전이면 날짜 범위 조건에 매칭되어 true를 반환한다")
    void createdDateBeforeToDateMatches() {
        post.publish();
        LocalDateTime toDate = LocalDateTime.now().plusDays(1);
        PostSearchRequest request = new PostSearchRequest(
                null, null, null, null, null, null, null, toDate);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("생성일시가 날짜 범위를 벗어나면 false를 반환한다")
    void createdDateOutsideDateRangeDoesNotMatch() {
        post.publish();
        LocalDateTime fromDate = LocalDateTime.now().plusDays(1);
        LocalDateTime toDate = LocalDateTime.now().plusDays(7);
        PostSearchRequest request = new PostSearchRequest(
                null, null, null, null, null, null, fromDate, toDate);

        boolean matches = post.matchesSearchRequest(request);

        assertThat(matches).isFalse();
    }
}
