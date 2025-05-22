package dooya.see.post.application;

import dooya.see.common.PostFixture;
import dooya.see.common.UserFixture;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.impl.PostCreateServiceImpl;
import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import dooya.see.user.domain.User;
import dooya.see.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PostCreateServiceTest {

    @InjectMocks
    private PostCreateServiceImpl postCreateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;


    @DisplayName("게시글 작성 성공 단위테스트")
    @Test
    void user_post_success() {
        // Arrange
        PostCommand command = PostFixture.command();
        User testUser = UserFixture.testUser();
        Post testPost = PostFixture.testPost();

        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // Act
        PostResult result = postCreateService.createPost(testUser.getEmail(), command);

        // Assert
        assertAll(
                () -> assertThat(result.nickName()).isEqualTo(testUser.getNickName()),
                () -> assertThat(result.title()).isEqualTo(command.title()),
                () -> assertThat(result.content()).isEqualTo(command.content())
        );
    }
}
