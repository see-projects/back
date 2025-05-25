package dooya.see.post.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.post.application.service.PostQueryService;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.PostCreateService;
import dooya.see.post.presentation.dto.PostPresentationMapper;
import dooya.see.post.presentation.dto.PostRequest;
import dooya.see.post.presentation.dto.PostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static dooya.see.post.presentation.dto.PostPresentationMapper.*;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostCreateService postCreateService;
    private final PostQueryService postQueryService;

    @PostMapping
    public ResponseEntity<PostResponse> post(@AuthenticationPrincipal LoginUser loginUser,
                                             @Valid @RequestBody PostRequest request) {
        String email = loginUser.getUsername();
        PostCommand command = toCommand(request);
        PostResult result = postCreateService.createPost(email, command);

        return ResponseEntity.created(URI.create("/api/post/" + result.id())).body(toResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPosts() {
        List<PostResult> posts = postQueryService.getPosts();

        return ResponseEntity.ok(PostPresentationMapper.toResponses(posts));
    }
}
