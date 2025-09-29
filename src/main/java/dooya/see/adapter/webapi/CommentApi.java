package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.CommentCreateResponse;
import dooya.see.adapter.webapi.dto.CommentDetailResponse;
import dooya.see.application.member.required.TokenManager;
import dooya.see.application.post.provided.CommentFinder;
import dooya.see.application.post.provided.CommentManager;
import dooya.see.domain.post.Comment;
import dooya.see.domain.post.dto.CommentCreateRequest;
import dooya.see.domain.post.dto.CommentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 관련 API를 제공하는 클래스
 */
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
        Long currentMemberId = extractCurrentMemberId(token);
        Comment comment = commentManager.create(request, postId, currentMemberId);

        return CommentCreateResponse.of(comment);
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentDetailResponse> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentFinder.findByPostId(postId);

        return convertToActiveComments(comments);
    }

    @GetMapping("/api/comments/{commentId}")
    public CommentDetailResponse getComment(@PathVariable Long commentId) {
        Comment comment = commentFinder.find(commentId);

        return CommentDetailResponse.of(comment);
    }

    @GetMapping("/api/comments/{parentCommentId}/replies")
    public List<CommentDetailResponse> getReplies(@PathVariable Long parentCommentId) {
        List<Comment> replies = commentFinder.findRepliesByParentId(parentCommentId);

        return convertToActiveComments(replies);
    }

    @GetMapping("/api/members/{memberId}/comments")
    public List<CommentDetailResponse> getCommentsByMember(@PathVariable Long memberId,
                                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        List<Comment> comments = commentFinder.findByMemberId(memberId);

        return filterCommentsByAccess(comments, memberId, token);
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentDetailResponse updateComment(@PathVariable Long commentId,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                               @RequestBody @Valid CommentUpdateRequest request) {
        Long currentMemberId = extractCurrentMemberId(token);
        Comment comment = commentManager.update(request, commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    @DeleteMapping("/api/comments/{commentId}/delete")
    public CommentDetailResponse deleteComment(@PathVariable Long commentId,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = extractCurrentMemberId(token);
        Comment comment = commentManager.delete(commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    @PatchMapping("/api/comments/{commentId}/hide")
    public CommentDetailResponse hideComment(@PathVariable Long commentId,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Long currentMemberId = extractCurrentMemberId(token);
        Comment comment = commentManager.hide(commentId, currentMemberId);

        return CommentDetailResponse.of(comment);
    }

    // Authentication 관련 메서드
    private Long extractCurrentMemberId(String token) {
        String extractedToken = AuthTokenExtractor.extractToken(token);
        return tokenManager.extractMemberIdFromToken(extractedToken);
    }

    private boolean isCurrentUser(Long memberId, String token) {
        if (!AuthTokenExtractor.isValidBearerToken(token))
            return false;

        Long currentMemberId = extractCurrentMemberId(token);
        return currentMemberId.equals(memberId);
    }

    // Response Conversion 관련 메서드
    private static List<CommentDetailResponse> convertToActiveComments(List<Comment> comments) {
        return comments.stream()
                .filter(Comment::canBeModified)
                .map(CommentDetailResponse::of)
                .toList();
    }

    private List<CommentDetailResponse> convertAllComments(List<Comment> comments) {
        return comments.stream()
                .map(CommentDetailResponse::of)
                .toList();
    }

    private List<CommentDetailResponse> filterCommentsByAccess(List<Comment> comments, Long memberId, String token) {
        if (isCurrentUser(memberId, token)) {
            return convertAllComments(comments);  // 본인: 모든 상태 반환
        }
        return convertToActiveComments(comments); // 타인: 활성 상태만 반환
    }
}
