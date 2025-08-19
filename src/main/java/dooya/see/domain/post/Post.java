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

        if (request.publishImmediately()) {
            post.status = PostStatus.PUBLISHED;
            post.metaData = PostMetaData.createPublished();
        } else {
            post.status = PostStatus.DRAFT;
            post.metaData = PostMetaData.create();
        }

        return post;
    }

    public void update(PostUpdateRequest request) {
        state(request.hasAnyUpdate(), "변경사항이 없습니다");

        if (request.title().isPresent() || request.body().isPresent()) {
            String newTitle = request.title().orElse(this.content.title());
            String newBody = request.body().orElse(this.content.body());

            this.content = new PostContent(newTitle, newBody);
        }

        if (request.category().isPresent()) {
            this.category = request.category().get();
        }

        this.metaData = this.metaData.updateModifiedAt();
    }

    public void publish() {
        if (this.status != PostStatus.DRAFT && this.status != PostStatus.HIDDEN) {
            throw new InvalidPostStatusTransitionException(this.status, "발행");
        }

        this.status = PostStatus.PUBLISHED;
        this.metaData = this.metaData.updatePublishedAt();
    }

    public void hide() {
        if (this.status == PostStatus.HIDDEN) {
            throw new InvalidPostStatusTransitionException(this.status, "숨김");
        }
        if (this.status == PostStatus.DELETED) {
            throw new InvalidPostStatusTransitionException(this.status, "숨김");
        }

        this.status = PostStatus.HIDDEN;
    }

    public void delete() {
        if (this.status == PostStatus.DELETED) {
            throw new InvalidPostStatusTransitionException(this.status, "삭제");
        }

        this.status = PostStatus.DELETED;
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void incrementViewCount() {
        if (status == PostStatus.PUBLISHED) {
            this.metaData = this.metaData.incrementViewCount();
        }
    }

    public void incrementLikeCount() {
        if (status == PostStatus.PUBLISHED) {
            this.metaData = this.metaData.incrementLikeCount();
        }
    }

    public void incrementCommentCount() {
        if (status == PostStatus.PUBLISHED) {
            this.metaData = this.metaData.incrementCommentCount();
        }
    }
}