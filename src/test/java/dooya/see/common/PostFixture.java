package dooya.see.common;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.domain.Post;
import dooya.see.post.presentation.dto.PostRequest;

public class PostFixture {

    public static PostRequest request() {
        return new PostRequest("테스트용 게시물 제목", "테스트용 게시물 내용입니다.");
    }

    public static PostCommand command() {
        return new PostCommand("테스트용 게시물 제목", "테스트용 게시물 내용입니다.");
    }

    public static PostResult result() {
        return new PostResult(
                1L,
                "testNickName",
                "테스트용 게시글 제목",
                "테스트용 게시물 내용입니다."
        );
    }

    public static Post testPost() {
        return new Post(
                1L,
                UserFixture.testUser(),
                "테스트용 게시글 제목",
                "테스트용 게시물 내용입니다."
        );
    }
}
