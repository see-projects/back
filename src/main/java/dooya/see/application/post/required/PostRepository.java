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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 게시물 관련 데이터 액세스를 담당하는 레포지토리 인터페이스
 */
public interface PostRepository extends Repository<Post, Long> {
    String BASELINE_WHERE_CLAUSE = """
            (:#{#request.keyword} IS NULL OR 
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
            """;

    String OPTIMIZED_WHERE_CLAUSE = """
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
            """;

    String ORDER_BY_CREATED_DESC = " ORDER BY p.metaData.createdAt DESC";

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
    Page<Post> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 지정된 카테고리에 속하는 게시물 목록을 조회합니다.
     *
     * @param category 조회할 게시물의 카테고리
     * @return 지정된 카테고리에 속하는 게시물 목록
     */
    Page<Post> findByCategory(PostCategory category, Pageable pageable);

    /**
     * 지정된 상태를 가진 게시물 목록을 조회합니다.
     *
     * @param status 조회할 게시물의 상태
     * @return 지정된 상태를 가진 게시물 목록
     */
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    /**
     * 지정된 카테고리와 상태를 가진 게시물 목록을 조회합니다.
     *
     * @param category 조회할 게시물의 카테고리
     * @param status 조회할 게시물의 상태
     * @return 지정된 카테고리와 상태를 가진 게시물 목록
     */
    Page<Post> findByCategoryAndStatus(PostCategory category, PostStatus status, Pageable pageable);

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
     * == STEP 0: Baseline ==
     * 전체 조건을 그대로 적용해 결과 목록을 반환합니다.
     * - LIKE 기반 키워드 검색과 모든 보조 조건을 한 번에 평가합니다.
     */
    @Query("""
        SELECT p FROM Post p 
        WHERE """ + BASELINE_WHERE_CLAUSE + ORDER_BY_CREATED_DESC)
    List<Post> search(@Param("request") PostSearchRequest request);

    /**
     * == STEP 1: Baseline + Pagination ==
     * STEP 0과 동일한 조건이지만 Page를 반환해 데이터 전송량을 줄입니다.
     */
    @Query("""
        SELECT p FROM Post p 
        WHERE """ + BASELINE_WHERE_CLAUSE + ORDER_BY_CREATED_DESC)
    Page<Post> searchWithPagination(
            @Param("request") PostSearchRequest request,
            Pageable pageable
    );

    /**
     * == STEP 2: Optimized Predicate Ordering ==
     * 인덱스를 활용할 수 있는 '=' 비교를 우선으로 평가한 뒤, 비용이 큰 LIKE 조건을 실행합니다.
     */
    @Query("""
        SELECT p FROM Post p 
        WHERE """ + OPTIMIZED_WHERE_CLAUSE + ORDER_BY_CREATED_DESC)
    Page<Post> searchWithOptimizedConditions(
            @Param("request") PostSearchRequest request,
            Pageable pageable
    );

    /**
     * == STEP 3: MySQL Full-text ==
     * MATCH … AGAINST 절을 활용해 MySQL의 전체 텍스트 인덱스를 사용합니다.
     * - MySQL 환경에서만 사용 가능합니다.
     */
    @Query(value = """
SELECT * FROM post p
WHERE 
    (:status IS NULL OR p.status = :status)
    AND (:category IS NULL OR p.category = :category)
    AND (:memberId IS NULL OR p.member_id = :memberId)
    AND (:fromDate IS NULL OR p.created_at >= :fromDate)
    AND (:toDate IS NULL OR p.created_at <= :toDate)
    AND (:keyword IS NULL OR MATCH(p.title, p.body) AGAINST (:keyword IN NATURAL LANGUAGE MODE))
ORDER BY p.created_at DESC
""",
            countQuery = """
    SELECT COUNT(*) FROM post p
    WHERE 
        (:status IS NULL OR p.status = :status)
        AND (:category IS NULL OR p.category = :category)
        AND (:memberId IS NULL OR p.member_id = :memberId)
        AND (:fromDate IS NULL OR p.created_at >= :fromDate)
        AND (:toDate IS NULL OR p.created_at <= :toDate)
        AND (:keyword IS NULL OR MATCH(p.title, p.body) AGAINST (:keyword IN NATURAL LANGUAGE MODE))
""",
            nativeQuery = true)
    Page<Post> searchWithFullTextIndex(
            @Param("status") String status,  // ← PostStatus → String
            @Param("category") String category,  // ← PostCategory → String
            @Param("memberId") Long memberId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    Page<Post> findAllBy(Pageable pageable);
}
