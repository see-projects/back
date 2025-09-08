package dooya.see.application.post.required;

import dooya.see.domain.post.Comment;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends Repository<Comment, Long> {
    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findByPostId(Long postId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByMemberId(Long memberId);
}
