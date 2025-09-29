package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;

/**
 * 게시물 관리를 위한 인터페이스
 */
public interface PostManager {
    /**
     * 새로운 게시물을 생성합니다.
     *
     * @param request 게시물 생성 요청 정보를 담고 있는 객체
     * @param memberId 게시물을 생성하는 회원의 식별자
     * @return 생성된 게시물 객체
     */
    Post create(PostCreateRequest request, Long memberId);

    /**
     * 게시물을 업데이트합니다.
     *
     * @param request 수정할 게시물 정보를 담고 있는 요청 객체
     * @param postId 수정하려는 게시물의 식별자
     * @param memberId 게시물을 수정하는 회원의 식별자
     * @return 업데이트된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 수정 권한이 없는 경우
     */
    Post update(PostUpdateRequest request, Long postId, Long memberId);

    /**
     * 게시물을 공개 상태로 변경합니다.
     *
     * @param postId 공개하려는 게시물의 식별자
     * @param memberId 게시물을 공개하는 회원의 식별자
     * @return 공개 상태로 변경된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 공개 권한이 없는 경우
     */
    Post publish(Long postId, Long memberId);

    /**
     * 게시물을 숨김 상태로 변경합니다.
     *
     * @param postId 숨기려는 게시물의 식별자
     * @param memberId 게시물을 숨기는 회원의 식별자
     * @return 숨김 상태로 변경된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 숨김 권한이 없는 경우
     */
    Post hide(Long postId, Long memberId);

    /**
     * 게시물을 삭제합니다.
     *
     * @param postId 삭제하려는 게시물의 식별자
     * @param memberId 게시물을 삭제하는 회원의 식별자
     * @return 삭제된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 삭제 권한이 없는 경우
     */
    Post delete(Long postId, Long memberId);

    /**
     * 게시물에 좋아요를 추가합니다.
     *
     * @param postId 좋아요를 추가할 게시물의 식별자
     * @param memberId 좋아요를 추가하는 회원의 식별자
     * @return 좋아요가 추가된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 좋아요를 추가할 권한이 없는 경우
     */
    Post likePost(Long postId, Long memberId);

    /**
     * 게시물의 좋아요를 취소합니다.
     *
     * @param postId 좋아요를 취소할 게시물의 식별자
     * @param memberId 좋아요를 취소하는 회원의 식별자
     * @return 좋아요가 취소된 게시물 객체
     * @throws dooya.see.domain.post.exception.PostNotFoundException 게시물을 찾을 수 없는 경우
     * @throws dooya.see.domain.post.exception.UnauthorizedPostAccessException 좋아요 취소 권한이 없는 경우
     */
    Post unlikePost(Long postId, Long memberId);
}