package dooya.see.domain.post;

public class PostFixture {
    public static PostCreateRequest createPostRequest() {
        return new PostCreateRequest("title", "content", PostCategory.TECH);
    }
}
