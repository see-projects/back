package dooya.see.post.infrastructure;

import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Builder
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public List<Post> findAll() {
        return postJpaRepository.findAll();
    }
}
