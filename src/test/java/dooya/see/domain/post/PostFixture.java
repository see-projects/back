package dooya.see.domain.post;

import java.util.Optional;

public class PostFixture {
    
    public static PostCreateRequest createPostRequest(boolean publishImmediately) {
        return new PostCreateRequest("테스트 게시글 제목입니다", "테스트 게시글 내용입니다", PostCategory.TECH, publishImmediately);
    }

    public static PostCreateRequest createPostRequest() {
        return createPostRequest(false);
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

    // ======================== PostSearchRequest Fixtures ========================
    
    /**
     * 빈 검색 조건 (모든 조건이 null)
     */
    public static PostSearchRequest emptySearchRequest() {
        return new PostSearchRequest(
                null, null, null, null, null, null, null, null
        );
    }
    
    /**
     * 키워드 통합 검색 요청
     */
    public static PostSearchRequest keywordSearchRequest(String keyword) {
        return new PostSearchRequest(
                keyword, null, null, null, null, null, null, null
        );
    }
    
    /**
     * 제목 전용 키워드 검색 요청
     */
    public static PostSearchRequest titleKeywordSearchRequest(String titleKeyword) {
        return new PostSearchRequest(
                null, titleKeyword, null, null, null, null, null, null
        );
    }
    
    /**
     * 내용 전용 키워드 검색 요청
     */
    public static PostSearchRequest contentKeywordSearchRequest(String contentKeyword) {
        return new PostSearchRequest(
                null, null, contentKeyword, null, null, null, null, null
        );
    }
    
    /**
     * 카테고리 필터 검색 요청
     */
    public static PostSearchRequest categorySearchRequest(PostCategory category) {
        return new PostSearchRequest(
                null, null, null, category, null, null, null, null
        );
    }
    
    /**
     * 작성자 필터 검색 요청
     */
    public static PostSearchRequest memberSearchRequest(Long memberId) {
        return new PostSearchRequest(
                null, null, null, null, memberId, null, null, null
        );
    }
    
    /**
     * 상태 필터 검색 요청
     */
    public static PostSearchRequest statusSearchRequest(PostStatus status) {
        return new PostSearchRequest(
                null, null, null, null, null, status, null, null
        );
    }
    
    /**
     * 날짜 범위 검색 요청
     */
    public static PostSearchRequest dateRangeSearchRequest(java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate) {
        return new PostSearchRequest(
                null, null, null, null, null, null, fromDate, toDate
        );
    }
    
    /**
     * 복합 조건 검색 요청 (키워드 + 카테고리)
     */
    public static PostSearchRequest keywordAndCategorySearchRequest(String keyword, PostCategory category) {
        return new PostSearchRequest(
                keyword, null, null, category, null, null, null, null
        );
    }
    
    /**
     * 복합 조건 검색 요청 (키워드 + 상태)
     */
    public static PostSearchRequest keywordAndStatusSearchRequest(String keyword, PostStatus status) {
        return new PostSearchRequest(
                keyword, null, null, null, null, status, null, null
        );
    }
    
    /**
     * 발행된 게시물만 검색하는 기본 요청
     */
    public static PostSearchRequest publishedPostsOnlyRequest() {
        return new PostSearchRequest(
                null, null, null, null, null, PostStatus.PUBLISHED, null, null
        );
    }
    
    /**
     * 커스텀 검색 요청 생성 헬퍼
     */
    public static PostSearchRequest customSearchRequest(
            String keyword, 
            String titleKeyword, 
            String contentKeyword,
            PostCategory category, 
            Long memberId, 
            PostStatus status,
            java.time.LocalDateTime fromDate, 
            java.time.LocalDateTime toDate) {
        return new PostSearchRequest(
                keyword, titleKeyword, contentKeyword, category, 
                memberId, status, fromDate, toDate
        );
    }
}
