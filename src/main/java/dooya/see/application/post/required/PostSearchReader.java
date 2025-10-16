package dooya.see.application.post.required;

import dooya.see.application.post.dto.PostSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchReader {
    Page<PostSearchResult> searchByKeyword(String keyword, Pageable pageable);
}
