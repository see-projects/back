package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * 전체 게시물 수를 조회합니다.
     *
     * @return 전체 게시물 수
     */
    @Query("SELECT COUNT(p) FROM Post p")
    long count();

    /**
     * 특정 카테고리의 게시물 수를 조회합니다.
     *
     * @param category 카테고리
     * @return 해당 카테고리의 게시물 수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.category = :category")
    long countByCategory(@Param("category") PostCategory category);

    /**
     * 특정 상태의 게시물 수를 조회합니다.
     *
     * @param status 게시물 상태
     * @return 해당 상태의 게시물 수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status")
    long countByStatus(@Param("status") PostStatus status);

    /**
     * 특정 작성자의 게시물 수를 조회합니다.
     *
     * @param memberId 작성자 ID
     * @return 해당 작성자의 게시물 수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.memberId = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);

    /**
     * == 원본 ==
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

    /**
     * == 1단계 ==
     * 주어진 검색 조건과 페이지 정보를 기반으로 게시물을 검색하고 페이징 처리된 결과를 반환합니다.
     *
     * @param request 검색 조건을 담고 있는 PostSearchRequest 객체
     * @param pageable 페이지 요청 정보를 담고 있는 Pageable 객체
     * @return 조건에 맞는 게시물 리스트와 페이징 정보를 포함하는 Page 객체
     * @throws IllegalArgumentException null이 아닌 필수 파라미터가 누락된 경우
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
    Page<Post> searchWithPagination(
            @Param("request") PostSearchRequest request,
            Pageable pageable
    );

    /**
     * == 2단계 ==
     * 게시물 목록을 최적화된 조건으로 검색합니다.
     *
     * @param request 게시물 검색 조건을 포함한 요청 객체
     * @param pageable 페이지네이션 정보를 포함한 객체
     * @return 검색 조건에 맞는 게시물의 페이지 데이터
     */
    @Query("""
        SELECT p FROM Post p 
        WHERE 
            (:#{#request.status} IS NULL OR p.status = :#{#request.status})
            AND (:#{#request.category} IS NULL OR p.category = :#{#request.category})
            AND (:#{#request.memberId} IS NULL OR p.memberId = :#{#request.memberId})
            AND (:#{#request.fromDate} IS NULL OR p.metaData.createdAt >= :#{#request.fromDate})
            AND (:#{#request.toDate} IS NULL OR p.metaData.createdAt <= :#{#request.toDate})
            AND (:#{#request.keyword} IS NULL OR 
                 (LOWER(p.content.title) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%')) OR 
                  LOWER(p.content.body) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%'))))
            AND (:#{#request.titleKeyword} IS NULL OR 
                 LOWER(p.content.title) LIKE LOWER(CONCAT('%', :#{#request.titleKeyword}, '%')))
            AND (:#{#request.contentKeyword} IS NULL OR 
                 LOWER(p.content.body) LIKE LOWER(CONCAT('%', :#{#request.contentKeyword}, '%')))
        ORDER BY p.metaData.createdAt DESC
        """)
    Page<Post> searchWithOptimizedConditions(
            @Param("request") PostSearchRequest request,
            Pageable pageable
    );
}