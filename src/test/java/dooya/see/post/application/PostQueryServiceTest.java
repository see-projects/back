package dooya.see.post.application;

import dooya.see.common.PostFixture;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.impl.PostQueryServiceImpl;
import dooya.see.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class PostQueryServiceTest {

    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Mock
    private PostRepository postRepository;

    @DisplayName("게시글 조회 성공 테스트")
    @Test
    void getPosts_shouldReturnList_whenPostsExist() {
        // given
        given(postRepository.findAll()).willReturn(PostFixture.testPosts());

        // when
        List<PostResult> result = postQueryService.getPosts();

        // then
        assertAll(
                () -> assertThat(result).hasSize(3),
                () -> assertThat(result.getFirst().title()).isEqualTo("테스트용 게시글 제목 1"),
                () -> assertThat(result.getFirst().nickName()).isEqualTo("testNickName"),
                () -> assertThat(result.get(1).content()).contains("테스트용 게시물 내용 2입니다."),
                () -> assertThat(result.get(2).id()).isNotNull()
        );
    }
}
