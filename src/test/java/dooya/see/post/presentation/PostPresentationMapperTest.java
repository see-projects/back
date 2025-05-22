package dooya.see.post.presentation;

import dooya.see.common.PostFixture;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.presentation.dto.PostRequest;
import dooya.see.post.presentation.dto.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dooya.see.common.PostFixture.result;
import static dooya.see.post.presentation.dto.PostPresentationMapper.toCommand;
import static dooya.see.post.presentation.dto.PostPresentationMapper.toResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class PostPresentationMapperTest {

    @DisplayName("PostRequest를 PostCommand로 정상 변환한다")
    @Test
    void convert_PostRequest_to_PostCommand() {
        // Arrange
        PostRequest request = PostFixture.request();

        // Act
        PostCommand command = toCommand(request);

        // Assert
        assertAll(
                () -> assertThat(command.title()).isEqualTo(request.title()),
                () -> assertThat(command.content()).isEqualTo(request.content())
        );
    }

    @DisplayName("PostResult를 PostResponse로 정상 변환한다")
    @Test
    void convert_PostResult_to_PostResponse() {
        // Arrange
        PostResult result = result();

        // Act
        PostResponse response = toResponse(result);

        // Assert
        assertAll(
                () -> assertThat(response.id()).isEqualTo(result.id()),
                () -> assertThat(response.nickName()).isEqualTo(result.nickName()),
                () -> assertThat(response.title()).isEqualTo(result.title()),
                () -> assertThat(response.content()).isEqualTo(result.content())
        );
    }
}
