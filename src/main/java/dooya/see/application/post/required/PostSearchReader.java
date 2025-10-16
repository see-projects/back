package dooya.see.application.post.required;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchReader {
    Page<PostDocument> searchByKeyword(String keyword, Pageable pageable);
}
