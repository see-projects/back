package dooya.see.application.post.required;

import dooya.see.domain.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    void deleteByPostIdAndMemberId(Long postId, Long memberId);
    
    long countByPostId(Long postId);
}
