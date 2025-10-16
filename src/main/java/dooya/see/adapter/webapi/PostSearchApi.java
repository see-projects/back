package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.PostSearchRequest;
import dooya.see.adapter.webapi.dto.PostSearchResponse;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.application.post.provided.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostSearchApi {
    private final PostFinder postFinder;

    @GetMapping("/elasticsearch")
    public Page<PostSearchResponse> search(PostSearchRequest request) {
        Page<PostSearchResult> result = postFinder.searchPosts(request.keyword(), request.toPageable());
        return result.map(PostSearchResponse::from);
    }
}
