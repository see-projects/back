package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.PostManager;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostApi {
    private final PostManager postManager;
    private final TokenManager tokenManager;

    @PostMapping("/api/posts")
    public PostCreateResponse createPost(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                         @RequestBody @Valid PostCreateRequest request) {
        Long currentMemberId = getCurrentMemberId(token);
        Post post = postManager.create(request, currentMemberId);

        return PostCreateResponse.of(post);
    }

    private Long getCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }
}
