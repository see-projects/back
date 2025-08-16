package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.PostFinder;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

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

    private Long getCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }

    private Post processViewCountIncrement(Long id, String token, Post post) {
        if (token != null) {
            Long currentMemberId = getCurrentMemberId(token);
            if (!post.isWrittenBy(currentMemberId)) {
                postManager.incrementViewCount(id);
                post = postFinder.find(id);
            }
        } else {
            postManager.incrementViewCount(id);
            post = postFinder.find(id);
        }
        return post;
    }
}
