package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.PostStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 게시물 관련 데이터 액세스를 담당하는 레포지토리 인터페이스
 */
public interface PostRepository extends Repository<Post, Long> {
    /**
     * 게시물을 저장합니다.
     *
     * @param post 저장할 게시물 객체
     * @return 저장된 게시물 객체
     */
    Post save(Post post);

    /**
     * ID로 게시물을 조회합니다.
     *
     * @param postId 조회할 게시물의 ID
     * @return 지정된 ID를 가진 게시물의 Optional 객체. 게시물이 존재하지 않을 경우 빈 Optional 반환
     */
    Optional<Post> findById(Long postId);

    /**
     * 주어진 회원 ID로 게시물을 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 지정된 회원 ID를 가진 게시물 목록
     */
    List<Post> findByMemberId(Long memberId);

    /**
     * 지정된 카테고리에 속하는 게시물 목록을 조회합니다.
     *
     * @param category 조회할 게시물의 카테고리
     * @return 지정된 카테고리에 속하는 게시물 목록
     */
    List<Post> findByCategory(PostCategory category);

    /**
     * 지정된 상태를 가진 게시물 목록을 조회합니다.
     *
     * @param status 조회할 게시물의 상태
     * @return 지정된 상태를 가진 게시물 목록
     */
    List<Post> findByStatus(PostStatus status);

    /**
     * 지정된 카테고리와 상태를 가진 게시물 목록을 조회합니다.
     *
     * @param category 조회할 게시물의 카테고리
     * @param status 조회할 게시물의 상태
     * @return 지정된 카테고리와 상태를 가진 게시물 목록
     */
    List<Post> findByCategoryAndStatus(PostCategory category, PostStatus status);

    /**
     * 검색 조건에 따라 게시물 목록을 조회합니다.
     *
     * @param request 검색 조건이 포함된 요청 객체
     * @return 조건에 맞는 게시물 목록
     */
    @Query("""
        SELECT p FROM Post p 
        WHERE (:#{#request.keyword} IS NULL OR 
               (LOWER(p.content.title) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%')) OR 
                LOWER(p.content.body) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%'))))
        AND (:#{#request.titleKeyword} IS NULL OR 
             LOWER(p.content.title) LIKE LOWER(CONCAT('%', :#{#request.titleKeyword}, '%')))
        AND (:#{#request.contentKeyword} IS NULL OR 
             LOWER(p.content.body) LIKE LOWER(CONCAT('%', :#{#request.contentKeyword}, '%')))
        AND (:#{#request.category} IS NULL OR p.category = :#{#request.category})
        AND (:#{#request.memberId} IS NULL OR p.memberId = :#{#request.memberId})
        AND (:#{#request.status} IS NULL OR p.status = :#{#request.status})
        AND (:#{#request.fromDate} IS NULL OR p.metaData.createdAt >= :#{#request.fromDate})
        AND (:#{#request.toDate} IS NULL OR p.metaData.createdAt <= :#{#request.toDate})
        ORDER BY p.metaData.createdAt DESC
        """)
    List<Post> search(@Param("request") PostSearchRequest request);
}