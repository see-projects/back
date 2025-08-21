package dooya.see.domain.post;

import lombok.Builder;

@Builder
public record PostWithStats(
        Post post,
        PostStats stats
) {
    public static PostWithStats of(Post post, PostStats stats) {
        return PostWithStats.builder()
                .post(post)
                .stats(stats != null ? stats : PostStats.create(post.getId()))
                .build();
    }

    public Long getPostId() {
        return post.getId();
    }

    public String getTitle() {
        return post.getContent().title();
    }

    public String getBody() {
        return post.getContent().body();
    }

    public PostCategory getCategory() {
        return post.getCategory();
    }

    public PostStatus getStatus() {
        return post.getStatus();
    }

    public Long getMemberId() {
        return post.getMemberId();
    }

    public Integer getViewCount() {
        return stats().getViewCount();
    }

    public Integer getLikeCount() {
        return stats().getLikeCount();
    }

    public Integer getCommentCount() {
        return stats().getCommentCount();
    }
}
