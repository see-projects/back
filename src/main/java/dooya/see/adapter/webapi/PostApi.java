package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.*;
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
        Long currentMemberId = getCurrentMemberId(token);
        Post post = postManager.create(request, currentMemberId);

        return PostCreateResponse.of(post);
    }

    @GetMapping("/api/posts/{id}")
    public PostDetailResponse getPost(@PathVariable Long id,
                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Post post = postFinder.find(id);

        validatePostAccessPermission(token, post);

        post = processViewCountIncrement(id, token, post);

        return PostDetailResponse.of(post);
    }

    @PutMapping("/api/posts/{id}")
    public PostDetailResponse updatePost(@PathVariable Long id,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                         @RequestBody @Valid PostUpdateRequest request) {
        Long currentMemberId = getCurrentMemberId(token);

        Post post = postManager.update(request, id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/publish")
    public PostDetailResponse publishPost(@PathVariable Long id,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = getCurrentMemberId(token);

        Post post = postManager.publish(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/hide")
    public PostDetailResponse hidePost(@PathVariable Long id,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = getCurrentMemberId(token);

        Post post = postManager.hide(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @PostMapping("/api/posts/{id}/delete")
    public PostDetailResponse deletePost(@PathVariable Long id,
                                         @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = getCurrentMemberId(token);

        Post post = postManager.delete(id, currentMemberId);

        return PostDetailResponse.of(post);
    }

    @GetMapping("/api/posts")
    public List<PostDetailResponse> getPublicPosts() {
        List<Post> posts = postFinder.findPublicPosts();

        return posts.stream()
                .map(PostDetailResponse::of)
                .toList();
    }

    @GetMapping("/api/posts/category/{category}")
    public List<PostDetailResponse> getPostsByCategory(@PathVariable PostCategory category) {
        List<Post> posts = postFinder.findPublicPostsByCategory(category);

        return posts.stream()
                .map(PostDetailResponse::of)
                .toList();
    }

    @GetMapping("/api/members/{memberId}/posts")
    public List<PostDetailResponse> getPostsByMember(@PathVariable Long memberId,
                                                     @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        if (AuthTokenExtractor.isValidBearerToken(token)) {
            Long currentMemberId = getCurrentMemberId(token);
            if (currentMemberId.equals(memberId)) {
                List<Post> posts = postFinder.findByMemberId(memberId);
                return posts.stream()
                        .map(PostDetailResponse::of)
                        .toList();
            }
        }

        List<Post> posts = postFinder.findByMemberId(memberId);
        return posts.stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLISHED)
                .map(PostDetailResponse::of)
                .toList();
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
    public List<PostDetailResponse> searchPosts(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) String contentKeyword,
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) PostStatus status) {
        boolean authenticated = AuthTokenExtractor.isValidBearerToken(token);
        Long currentMemberId = authenticated ? getCurrentMemberId(token) : null;

        PostStatus effectiveStatus = status;
        if (!authenticated) {
            effectiveStatus = PostStatus.PUBLISHED;
        } else if (memberId == null || !memberId.equals(currentMemberId)) {
            effectiveStatus = PostStatus.PUBLISHED;
        }

        PostSearchRequest searchRequest = PostSearchRequest.builder()
                .keyword(keyword)
                .titleKeyword(titleKeyword)
                .contentKeyword(contentKeyword)
                .category(category)
                .memberId(memberId)
                .status(effectiveStatus)
                .build();
                
        List<Post> posts = postFinder.search(searchRequest);
        
        return posts.stream()
                .map(PostDetailResponse::of)
                .toList();
    }

    private Long getCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }

    private Post processViewCountIncrement(Long id, String token, Post post) {
        if (AuthTokenExtractor.isValidBearerToken(token)) {
            Long currentMemberId = getCurrentMemberId(token);
            if (!post.isWrittenBy(currentMemberId)) {
//                postManager.incrementViewCount(id);
                post = postFinder.find(id);
            }
        } else {
//            postManager.incrementViewCount(id);
            post = postFinder.find(id);
        }
        return post;
    }

    private void validatePostAccessPermission(String token, Post post) {
        if (!AuthTokenExtractor.isValidBearerToken(token)) {
            if (post.getStatus() != PostStatus.PUBLISHED) {
                throw new UnauthorizedPostAccessException("게시글을 조회할 권한이 없습니다");
            }
        } else {
            Long currentMemberId = getCurrentMemberId(token);
            if (!post.isWrittenBy(currentMemberId) && post.getStatus() != PostStatus.PUBLISHED) {
                throw new UnauthorizedPostAccessException("게시글을 조회할 권한이 없습니다");
            }
        }
    }
}
