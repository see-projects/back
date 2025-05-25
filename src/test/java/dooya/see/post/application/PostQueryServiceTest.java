package dooya.see.post.application;

import dooya.see.common.PostFixture;
import dooya.see.common.exception.CustomException;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.application.service.impl.PostQueryServiceImpl;
import dooya.see.post.domain.Post;
import dooya.see.post.domain.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        Page<Post> page = new PageImpl<>(List.of(PostFixture.testPost()));
        given(postRepository.findAll(any(Pageable.class))).willReturn(page);

        // when
        Page<PostResult> result = postQueryService.getPosts(PageRequest.of(0, 10));

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.getContent().getFirst().title()).isEqualTo("테스트용 게시글 제목"),
                () -> assertThat(result.getContent().getFirst().nickName()).isEqualTo("testNickName"),
                () -> assertThat(result.getContent().getFirst().content()).contains("테스트용 게시물 내용입니다.")
        );
    }

    @DisplayName("단일 게시글 조회 실패 테스트 - 게시글이 존재하지 않을 경우")
    @Test
    void getPost_shouldReturn_whenPostExistFail() {
        // given
        Long id = 1L;

        // then
        assertThatCode(() -> postQueryService.getPost(id))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글이 존재하지 않습니다.");
    }

    @DisplayName("단일 게시글 조회 성공 테스트")
    @Test
    void getPost_shouldReturn_whenPostExistSuccess() {
        // given
        Long id = 1L;
        given(postRepository.findById(id)).willReturn(Optional.of(PostFixture.testPost()));

        // when
        PostResult result = postQueryService.getPost(id);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.nickName()).isEqualTo("testNickName"),
                () -> assertThat(result.title()).isEqualTo("테스트용 게시글 제목"),
                () -> assertThat(result.content()).isEqualTo("테스트용 게시물 내용입니다.")
        );
    }
}
