package dooya.see.application.post;

import dooya.see.application.post.provided.CommentFinder;
import dooya.see.application.post.required.CommentRepository;
import dooya.see.domain.post.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentQueryService implements CommentFinder {
    private final CommentRepository commentRepository;

    @Override
    public Comment find(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
    }
}
