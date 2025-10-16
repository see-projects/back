package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.dto.PostSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시물을 검색하기 위한 인터페이스입니다.
 */
public interface PostFinder {
    /**
     * 지정된 ID로 게시물을 검색합니다.
     *
     * @param postId 검색할 게시물의 ID
     * @return 게시물 객체를 반환
     */
    Post find(Long postId);

    /**
     * 특정 회원 ID를 기준으로 게시물 목록을 검색합니다.
     *
     * @param memberId 검색할 회원의 ID
     * @return 회원 ID에 해당하는 게시물 목록
     */
    Page<Post> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 카테고리를 기준으로 게시물 목록을 검색합니다.
     *
     * @param category 검색할 게시물의 카테고리
     * @return 카테고리에 해당하는 게시물 목록
     */
    Page<Post> findByCategory(PostCategory category, Pageable pageable);

    /**
     * 지정된 상태에 따라 게시물 목록을 검색합니다.
     *
     * @param status 검색할 게시물의 상태
     * @return 상태에 해당하는 게시물 목록
     */
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    /**
     * 공개 상태인 게시물 목록을 검색합니다.
     *
     * @return 공개 게시물의 목록
     */
    Page<Post> findPublicPosts(Pageable pageable);

    /**
     * 특정 카테고리에 해당하는 공개 상태의 게시물 목록을 검색합니다.
     *
     * @param category 검색할 게시물의 카테고리
     * @return 카테고리에 해당하는 공개 게시물 목록
     */
    Page<Post> findPublicPostsByCategory(PostCategory category, Pageable pageable);

    /**
     * 게시물을 검색합니다.
     *
     * @param searchRequest 검색 요청 조건을 담은 객체
     * @return 검색 조건에 해당하는 게시물 목록
     */
    Page<Post> search(PostSearchRequest searchRequest, Pageable pageable);

    /**
     * 특정 게시물을 조회합니다.
     *
     * @param postId 조회할 게시물의 ID
     * @param viewerId 게시물을 조회하는 사용자의 ID
     * @return 조회된 게시물 객체
     */
    Post viewPost(Long postId, Long viewerId);

    Page<Post> findPosts(PostSearchRequest searchRequest, Pageable pageable);

    Page<Post> searchPosts(String keyword, Pageable pageable);
}