package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.state;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends AbstractEntity {
    @Embedded
    private PostContent content;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Embedded
    private PostMetaData metaData;

    public static Post create(PostCreateRequest request, Long memberId) {
        Post post = new Post();

        post.content = new PostContent(request.title(), request.body());
        post.memberId = requireNonNull(memberId);
        post.category = requireNonNull(request.category());
        post.status = PostStatus.DRAFT;
        post.metaData = PostMetaData.create();

        return post;
    }

    public void publish() {
        state(this.status == PostStatus.DRAFT, "DRAFT 상태가 아닙니다");

        this.status = PostStatus.PUBLISHED;
        this.metaData = this.metaData.updatePublishedAt();
    }
}