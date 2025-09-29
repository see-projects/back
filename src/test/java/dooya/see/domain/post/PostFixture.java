package dooya.see.domain.post;

import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;

import java.util.List;
import java.util.Optional;

public class PostFixture {
    
    public static PostCreateRequest createPostRequest(boolean publishImmediately) {
        return new PostCreateRequest("테스트 게시글 제목입니다", "테스트 게시글 내용입니다", PostCategory.TECH, publishImmediately, List.of("Spring", "Backend", "Java"));
    }

    public static PostCreateRequest createPostRequest() {
        return createPostRequest(false);
    }

    public static PostCreateRequest createPostRequest(String title, String body) {
        return new PostCreateRequest(title, body, PostCategory.TECH, true, List.of("Spring", "Backend", "Java"));
    }

    public static PostUpdateRequest updateAllFieldsRequest() {
        return new PostUpdateRequest(
                Optional.of("수정된 제목"),
                Optional.of("수정된 내용"),
                Optional.of(PostCategory.QNA),
                Optional.of(List.of("Spring", "SpringBoot", "Java"))
        );
    }

    public static PostUpdateRequest updateTitleOnlyRequest() {
        return new PostUpdateRequest(
                Optional.of("새로운 제목"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static PostUpdateRequest updateBodyOnlyRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.of("새로운 내용"),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static PostUpdateRequest updateCategoryOnlyRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.of(PostCategory.NOTICE),
                Optional.empty()
        );
    }

    public static PostUpdateRequest updateTitleAndCategoryRequest() {
        return new PostUpdateRequest(
                Optional.of("제목과 카테고리 변경"),
                Optional.empty(),
                Optional.of(PostCategory.GENERAL),
                Optional.empty()
        );
    }

    public static PostUpdateRequest noUpdateRequest() {
        return new PostUpdateRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
    
    // 커스텀 업데이트 요청 생성 헬퍼
    public static PostCreateRequest createPostRequestWithCategory(String title, String body, PostCategory category) {
        return new PostCreateRequest(title, body, category, true, List.of("Spring", "Backend", "Java"));
    }

    public static PostCreateRequest createPostRequestWithCategory(String title, String body, PostCategory category, boolean publishImmediately) {
        return new PostCreateRequest(title, body, category, publishImmediately, List.of("Spring", "Backend", "Java"));
    }

    public static PostCreateRequest createDraftPostRequest(String title, String body) {
        return new PostCreateRequest(title, body, PostCategory.TECH, false, List.of("Spring", "Backend", "Java"));
    }

    public static Post createPublishedWithCategory(String title, String body, PostCategory category) {
        PostCreateRequest request = new PostCreateRequest(
                title,
                body,
                category,
                true,
                List.of("Spring", "Backend", "Java")
        );

        return Post.create(request, 1L);
    }
}
