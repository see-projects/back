package dooya.see.common;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.post.domain.Post;
import dooya.see.post.presentation.dto.PostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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

    public static Page<PostResult> pageResult() {
        List<PostResult> content = List.of(
                new PostResult(1L, "작성자1", "제목1", "내용1"),
                new PostResult(2L, "작성자2", "제목2", "내용2")
        );

        return new PageImpl<>(content, PageRequest.of(0, 10), content.size());
    }

    public static Post testPost() {
        return new Post(
                1L,
                UserFixture.testUser(),
                "테스트용 게시글 제목",
                "테스트용 게시물 내용입니다."
        );
    }

    public static List<Post> testPosts() {
        return List.of(
                new Post(90L, UserFixture.testUser(), "테스트용 게시글 제목 1", "테스트용 게시물 내용 1입니다."),
                new Post(91L, UserFixture.testUser(), "테스트용 게시글 제목 2", "테스트용 게시물 내용 2입니다."),
                new Post(92L, UserFixture.testUser(), "테스트용 게시글 제목 3", "테스트용 게시물 내용 3입니다.")
        );
    }
}
