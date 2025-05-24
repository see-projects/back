package dooya.see.post.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.BDDMockito.*;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    private PostQueryService postQueryService;

    @Test
    void name() {
        // given
        // when
        // then
        then(postQueryService).should().getPosts();
    }
}
