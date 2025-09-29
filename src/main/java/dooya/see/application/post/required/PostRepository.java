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
 * 게시물 저장소 Secondary Port
 */
public interface PostRepository extends Repository<Post, Long> {
    Post save(Post post);

    Optional<Post> findById(Long postId);

    List<Post> findByMemberId(Long memberId);

    List<Post> findByCategory(PostCategory category);

    List<Post> findByStatus(PostStatus status);

    List<Post> findByCategoryAndStatus(PostCategory category, PostStatus status);

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