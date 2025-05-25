package dooya.see.post.application;

import dooya.see.common.PostFixture;
import dooya.see.common.exception.CustomException;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.PostQueryService;
import dooya.see.post.application.service.impl.PostQueryServiceImpl;
import dooya.see.post.domain.Post;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostQueryServiceTest {

    private final PostQueryService postQueryService = new PostQueryServiceImpl();

    @DisplayName("게시글 조회 실패 테스트 - 게시글이 없는 경우")
    @Test
    void noPost() {
        assertThatCode(postQueryService::getPosts)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글이 존재하지 않습니다.");
    }


//    @Test
//    void name() {
//        // Arrange
//        Post post = PostFixture.testPost();
//
//        // Act
//        List<PostResult> result = postQueryService.getPosts();
//
//        // Assert
//        assertThat(result.get(0).id()).isEqualTo(post.getId());
//    }
}
