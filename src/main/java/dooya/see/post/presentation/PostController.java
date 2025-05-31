package dooya.see.post.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.post.application.service.PostQueryService;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.PostCreateService;
import dooya.see.post.presentation.dto.PostPresentationMapper;
import dooya.see.post.presentation.dto.PostRequest;
import dooya.see.post.presentation.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static dooya.see.post.presentation.dto.PostPresentationMapper.*;

@Tag(name = "Post", description = "게시글 관련 API입니다.")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostCreateService postCreateService;
    private final PostQueryService postQueryService;

    @Operation(
            summary = "게시글 작성",
            description = "로그인한 사용자가 새 게시글을 작성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인하지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PostResponse> post(@AuthenticationPrincipal LoginUser loginUser,
                                             @Valid @RequestBody PostRequest request) {
        String email = loginUser.getUsername();
        PostCommand command = toCommand(request);
        PostResult result = postCreateService.createPost(email, command);

        return ResponseEntity.created(URI.create("/api/post/" + result.id())).body(toResponse(result));
    }

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지네이션된 게시글 목록 조회 성공."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(Pageable pageable) {
        Page<PostResult> posts = postQueryService.getPosts(pageable);

        return ResponseEntity.ok(PostPresentationMapper.toResponses(posts));
    }

    @Operation(
            summary = "게시글 단건 조회",
            description = "게시글 ID를 기준으로 게시글 하나를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResult result = postQueryService.getPost(id);

        return ResponseEntity.ok(PostPresentationMapper.toResponse(result));
    }
}
