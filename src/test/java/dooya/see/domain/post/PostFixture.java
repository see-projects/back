package dooya.see.domain.post;

import java.util.Optional;

public class PostFixture {
    
    public static PostCreateRequest createPostRequest(boolean publishImmediately) {
        return new PostCreateRequest("테스트 게시글 제목입니다", "테스트 게시글 내용입니다", PostCategory.TECH, publishImmediately);
    }

    public static PostCreateRequest createPostRequest() {
        return createPostRequest(false);
    }

    public static PostCreateRequest createPostRequest(String title, String body) {
        return new PostCreateRequest(title, body, PostCategory.TECH, true);
    }

    public static PostUpdateRequest updateAllFieldsRequest() {
        return new PostUpdateRequest(
                Optional.of("수정된 제목"),
                Optional.of("수정된 내용"),
                Optional.of(PostCategory.QNA)
        );
    }

    public static PostUpdateRequest updateTitleOnlyRequest() {
        return new PostUpdateRequest(
                Optional.of("새로운 제목"),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static PostUpdateRequest updateBodyOnlyRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.of("새로운 내용"),
                Optional.empty()
        );
    }

    public static PostUpdateRequest updateCategoryOnlyRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.of(PostCategory.NOTICE)
        );
    }

    public static PostUpdateRequest updateTitleAndCategoryRequest() {
        return new PostUpdateRequest(
                Optional.of("제목과 카테고리 변경"),
                Optional.empty(),
                Optional.of(PostCategory.GENERAL)
        );
    }

    public static PostUpdateRequest noUpdateRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
    
    // 커스텀 업데이트 요청 생성 헬퍼
    public static PostUpdateRequest customUpdateRequest(String title, String body, PostCategory category) {
        return new PostUpdateRequest(
                title != null ? Optional.of(title) : Optional.empty(),
                body != null ? Optional.of(body) : Optional.empty(),
                category != null ? Optional.of(category) : Optional.empty()
        );
    }

    public static Post createPublishedPost(String title, String body) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                PostCategory.TECH,
                true
        );

        return Post.create(request, 1L);
    }

    public static Post createDraftPost(String title, String body) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                PostCategory.TECH,
                false
        );

        return Post.create(request, 1L);
    }

    public static Post createPublishedPostWithCategory(String title, String body, PostCategory category) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                category,
                true
        );

        return Post.create(request, 1L);
    }

    public static Post createPostWithMember(String title, String body, Long memberId, boolean publishImmediately) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                PostCategory.TECH,
                publishImmediately
        );

        return Post.create(request, memberId);
    }

    public static PostCreateRequest createPostRequestWithCategory(String title, String body, PostCategory category) {
        return new PostCreateRequest(title, body, category, true);
    }

    public static PostCreateRequest createPostRequestWithCategory(String title, String body, PostCategory category, boolean publishImmediately) {
        return new PostCreateRequest(title, body, category, publishImmediately);
    }

    public static PostCreateRequest createDraftPostRequest(String title, String body) {
        return new PostCreateRequest(title, body, PostCategory.TECH, false);
    }

    public static Post createPublishedWithCategory(String title, String body, PostCategory category) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                category,
                true
        );

        return Post.create(request, 1L);
    }
}
