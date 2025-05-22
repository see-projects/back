package dooya.see.common;

import dooya.see.post.application.PostRequest;

public class PostFixture {

    public static PostRequest request() {
        return new PostRequest("테스트용 게시물 제목", "테스트용 게시물 내용입니다.");
    }
}
