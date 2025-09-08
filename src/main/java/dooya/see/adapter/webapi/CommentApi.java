package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.CommentCreateResponse;
import dooya.see.adapter.webapi.dto.CommentDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.CommentFinder;
import dooya.see.application.post.provided.CommentManager;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.CommentCreateRequest;
import dooya.see.domain.post.CommentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentApi {
    private final CommentManager commentManager;
    private final CommentFinder commentFinder;
    private final TokenManager tokenManager;

    @PostMapping("/api/posts/{postId}/comments")
    public CommentCreateResponse createComment(@PathVariable Long postId,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                               @RequestBody @Valid CommentCreateRequest request) {
        Long currentMemberId = getCurrentMemberId(token);
        Comment comment = commentManager.create(request, postId, currentMemberId);

        return CommentCreateResponse.of(comment);
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentDetailResponse> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentFinder.findByPostId(postId);

        return comments.stream()
                .map(CommentDetailResponse::of)
                .toList();
    }

    @GetMapping("/api/comments/{commentId}")
    public CommentDetailResponse getComment(@PathVariable Long commentId) {
        Comment comment = commentFinder.find(commentId);

        return CommentDetailResponse.of(comment);
    }

    @GetMapping("/api/comments/{parentCommentId}/replies")
    public List<CommentDetailResponse> getReplies(@PathVariable Long parentCommentId) {
        List<Comment> replies = commentFinder.findRepliesByParentId(parentCommentId);

        return replies.stream()
                .map(CommentDetailResponse::of)
                .toList();
    }

    @GetMapping("/api/members/{memberId}/comments")
    public List<CommentDetailResponse> getCommentsByMember(@PathVariable Long memberId,
                                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        // 본인의 댓글 조회인 경우 모든 상태의 댓글 반환
        if (AuthTokenExtractor.isValidBearerToken(token)) {
            Long currentMemberId = getCurrentMemberId(token);
            if (currentMemberId.equals(memberId)) {
                List<Comment> comments = commentFinder.findByMemberId(memberId);
                return comments.stream()
                        .map(CommentDetailResponse::of)
                        .toList();
            }
        }

        // 다른 사람의 댓글 조회인 경우 활성 상태만 반환
        List<Comment> comments = commentFinder.findByMemberId(memberId);
        return comments.stream()
                .filter(Comment::canBeModified) // ACTIVE 상태만
                .map(CommentDetailResponse::of)
                .toList();
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentDetailResponse updateComment(@PathVariable Long commentId,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                               @RequestBody @Valid CommentUpdateRequest request) {
        Long currentMemberId = getCurrentMemberId(token);
        Comment comment = commentManager.update(request, commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    @PostMapping("/api/comments/{commentId}/delete")
    public CommentDetailResponse deleteComment(@PathVariable Long commentId,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = getCurrentMemberId(token);
        Comment comment = commentManager.delete(commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    @PostMapping("/api/comments/{commentId}/hide")
    public CommentDetailResponse hideComment(@PathVariable Long commentId,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = getCurrentMemberId(token);
        Comment comment = commentManager.hide(commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    private Long getCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }
}
