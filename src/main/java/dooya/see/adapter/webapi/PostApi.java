package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.*;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostApi {
    private final PostManager postManager;
    private final TokenManager tokenManager;
    private final PostFinder postFinder;

    @PostMapping("/api/posts")
    public PostCreateResponse createPost(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                         @RequestBody @Valid PostCreateRequest request) {
        Long currentMemberId = extractCurrentMemberId(token);
        Post post = postManager.create(request, currentMemberId);

        return PostCreateResponse.of(post);
    }

    @GetMapping("/api/posts/{id}")
    public PostDetailResponse getPost(@PathVariable Long id,
                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Post post = findPostWithAccess(id, token);
        processViewIfNeeded(post, token);

        return PostDetailResponse.of(post);
    }

    @PutMapping("/api/posts/{id}")
    public PostDetailResponse updatePost(@PathVariable Long id,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                         @RequestBody @Valid PostUpdateRequest request) {
        Long currentMemberId = extractCurrentMemberId(token);
        Post post = postManager.update(request, id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/publish")
    public PostDetailResponse publishPost(@PathVariable Long id,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = extractCurrentMemberId(token);
        Post post = postManager.publish(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/hide")
    public PostDetailResponse hidePost(@PathVariable Long id,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = extractCurrentMemberId(token);
        Post post = postManager.hide(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/delete")
    public PostDetailResponse deletePost(@PathVariable Long id,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = extractCurrentMemberId(token);
        Post post = postManager.delete(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @GetMapping("/api/posts")
    public List<PostDetailResponse> getPublicPosts() {
        List<Post> posts = postFinder.findPublicPosts();

        return convertToResponses(posts);
    }

    @GetMapping("/api/posts/category/{category}")
    public List<PostDetailResponse> getPostsByCategory(@PathVariable PostCategory category) {
        List<Post> posts = postFinder.findPublicPostsByCategory(category);

        return convertToResponses(posts);
    }

    @GetMapping("/api/members/{memberId}/posts")
    public List<PostDetailResponse> getPostsByMember(@PathVariable Long memberId,
                                                     @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        List<Post> posts = postFinder.findByMemberId(memberId);

        return filterPostsByAccess(posts, memberId, token);
    }

    @GetMapping("/api/posts/status/{status}")
    public List<PostDetailResponse> getPostsByStatus(@PathVariable PostStatus status,
                                                     @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        // 관리자나 특별한 권한이 있는 경우에만 허용하는 것이 좋지만,
        // 일단 기본 구현으로 진행
        List<Post> posts = postFinder.findByStatus(status);

        return posts.stream()
                .map(PostDetailResponse::of)
                .toList();
    }

    @GetMapping("/api/posts/search")
    public List<PostDetailResponse> searchPosts(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String titleKeyword,
                                                @RequestParam(required = false) String contentKeyword,
                                                @RequestParam(required = false) PostCategory category,
                                                @RequestParam(required = false) Long memberId,
                                                @RequestParam(required = false) PostStatus status) {
        PostSearchRequest searchRequest = buildSearchRequest(token, keyword, titleKeyword, contentKeyword, category, memberId, status);
        List<Post> posts = postFinder.search(searchRequest);

        return convertToResponses(posts);
    }

    // Authentication 관련 메서드
    private Long extractCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }

    private boolean isAuthenticated(String token) {
        return AuthTokenExtractor.isValidBearerToken(token);
    }

    private boolean isCurrentUser(Long memberId, String token) {
        if (!isAuthenticated(token))
            return false;
        Long currentMemberId = extractCurrentMemberId(token);

        return currentMemberId.equals(memberId);
    }

    // Post Access 관련 메서드
    private Post findPostWithAccess(Long postId, String token) {
        Post post = postFinder.find(postId);
        validatePostAccess(post, token);

        return post;
    }

    private void validatePostAccess(Post post, String token) {
        if (!canAccessPost(post, token))
            throw new UnauthorizedPostAccessException("게시글을 조회할 권한이 없습니다");
    }

    private boolean canAccessPost(Post post, String token) {
        if (post.getStatus() == PostStatus.PUBLISHED)
            return true;  // 공개 게시글은 누구나 접근 가능

        if (!isAuthenticated(token))
            return false;  // 비공개 게시글은 인증 필요

        Long currentMemberId = extractCurrentMemberId(token);

        return post.isWrittenBy(currentMemberId);  // 작성자만 비공개 게시글 접근 가능
    }

    private void processViewIfNeeded(Post post, String token) {
        if (shouldIncrementView(post, token)) {
            //postManager.incrementViewCount(post.getId());  // 주석 해제 시 사용
        }
    }

    private boolean shouldIncrementView(Post post, String token) {
        if (!isAuthenticated(token))
            return true;  // 비인증 사용자는 항상 조회수 증가

        Long currentMemberId = extractCurrentMemberId(token);

        return !post.isWrittenBy(currentMemberId);  // 본인 게시글이 아닐 때만 조회수 증가
    }

    // Response Conversion 관련 메서드
    private List<PostDetailResponse> convertToResponses(List<Post> posts) {
        return posts.stream()
                .map(PostDetailResponse::of)
                .toList();
    }

    private List<PostDetailResponse> filterPostsByAccess(List<Post> posts, Long memberId, String token) {
        if (isCurrentUser(memberId, token))
            return convertToResponses(posts);  // 본인: 모든 상태 반환

        return posts.stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)  // 타인: 공개 게시글만
                .map(PostDetailResponse::of)
                .toList();
    }

    // Search 관련 Private 메서드
    private PostSearchRequest buildSearchRequest(String token, String keyword, String titleKeyword,
                                                 String contentKeyword, PostCategory category,
                                                 Long memberId, PostStatus status) {
        PostStatus effectiveStatus = determineEffectiveStatus(token, memberId, status);

        return PostSearchRequest.builder()
                .keyword(keyword)
                .titleKeyword(titleKeyword)
                .contentKeyword(contentKeyword)
                .category(category)
                .memberId(memberId)
                .status(effectiveStatus)
                .build();
    }

    private PostStatus determineEffectiveStatus(String token, Long memberId, PostStatus status) {
        if (!isAuthenticated(token))
            return PostStatus.PUBLISHED;  // 비인증 사용자는 공개 게시글만

        if (memberId != null && isCurrentUser(memberId, token))
            return status;  // 본인 게시글 검색은 모든 상태 허용

        return PostStatus.PUBLISHED;  // 타인 게시글 검색은 공개만
    }
}
