package dooya.see.post.infrastructure;

import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
    public Page<Post> findAll(Pageable pageable) {
        return postJpaRepository.findAll(pageable);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }
}
