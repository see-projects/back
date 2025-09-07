package dooya.see.application.post.provided;

import dooya.see.domain.post.Comment;

public interface CommentFinder {
    Comment find(Long commentId);
}
