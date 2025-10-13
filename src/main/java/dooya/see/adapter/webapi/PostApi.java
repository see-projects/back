package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 관리와 관련된 API를 제공하는 컨트롤러 클래스
 */
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
    public List<PostDetailResponse> getPublicPosts(
            @PageableDefault(size = 20, sort = "metaData.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> posts = postFinder.findPublicPosts(pageable);

        return mapToResponses(posts);
    }

    @GetMapping("/api/posts/category/{category}")
    public List<PostDetailResponse> getPostsByCategory(@PathVariable PostCategory category,
                                                       @PageableDefault(size = 20, sort = "metaData.createdAt", direction = Sort.Direction.DESC)
                                                       Pageable pageable) {
        Page<Post> posts = postFinder.findPublicPostsByCategory(category, pageable);

        return mapToResponses(posts);
    }

    @GetMapping("/api/members/{memberId}/posts")
    public List<PostDetailResponse> getPostsByMember(@PathVariable Long memberId,
                                                     @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                                     @PageableDefault(size = 20, sort = "metaData.createdAt", direction = Sort.Direction.DESC)
                                                     Pageable pageable) {
        if (isCurrentUser(memberId, token))
            return mapToResponses(postFinder.findByMemberId(memberId, pageable));

        PostSearchRequest request = PostSearchRequest.builder()
                .memberId(memberId)
                .status(PostStatus.PUBLISHED)
                .build();

        return mapToResponses(postFinder.search(request, pageable));
    }

    @GetMapping("/api/posts/status/{status}")
    public List<PostDetailResponse> getPostsByStatus(@PathVariable PostStatus status,
                                                     @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                                     @PageableDefault(size = 20, sort = "metaData.createdAt", direction = Sort.Direction.DESC)
                                                     Pageable pageable) {
        // 관리자나 특별한 권한이 있는 경우에만 허용하는 것이 좋지만,
        // 일단 기본 구현으로 진행
        Page<Post> posts = postFinder.findByStatus(status, pageable);

        return mapToResponses(posts);
    }

    @GetMapping("/api/posts/search")
    public List<PostDetailResponse> searchPosts(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String titleKeyword,
                                                @RequestParam(required = false) String contentKeyword,
                                                @RequestParam(required = false) PostCategory category,
                                                @RequestParam(required = false) Long memberId,
                                                @RequestParam(required = false) PostStatus status,
                                                @PageableDefault(size = 20, sort = "metaData.createdAt", direction = Sort.Direction.DESC)
                                                Pageable pageable) {
        PostSearchRequest searchRequest = buildSearchRequest(token, keyword, titleKeyword, contentKeyword, category, memberId, status);
        Page<Post> posts = postFinder.search(searchRequest, pageable);

        return mapToResponses(posts);
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
    private List<PostDetailResponse> mapToResponses(Page<Post> posts) {
        return posts.stream()
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
