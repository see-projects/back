package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.shared.AbstractAggregateRoot;
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
public class Post extends AbstractAggregateRoot {
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

        post.addDomainEvent(new PostCreated(
                post.getId(),
                memberId,
                post.category,
                request.publishImmediately()
        ));

        return post;
    }

    public void update(PostUpdateRequest request) {
        state(request.hasAnyUpdate(), "변경사항이 없습니다");

        boolean titleChanged = false;
        boolean bodyChanged = false;
        boolean categoryChanged = false;

        if (request.title().isPresent() || request.body().isPresent()) {
            String originalTitle = this.content.title();
            String originalBody = this.content.body();
            
            String newTitle = request.title().orElse(originalTitle);
            String newBody = request.body().orElse(originalBody);

            titleChanged = !originalTitle.equals(newTitle);
            bodyChanged = !originalBody.equals(newBody);

            this.content = new PostContent(newTitle, newBody);
        }

        if (request.category().isPresent()) {
            PostCategory originalCategory = this.category;
            this.category = request.category().get();
            categoryChanged = !originalCategory.equals(this.category);
        }

        this.metaData = this.metaData.updateModifiedAt();

        // 도메인 이벤트 발행
        this.addDomainEvent(new PostUpdated(
            this.getId(),
            this.memberId,
            titleChanged,
            bodyChanged,
            categoryChanged
        ));
    }

    public void publish() {
        if (this.status != PostStatus.DRAFT && this.status != PostStatus.HIDDEN) {
            throw new InvalidPostStatusTransitionException(this.status, "발행");
        }

        this.status = PostStatus.PUBLISHED;
        this.metaData = this.metaData.updatePublishedAt();

        this.addDomainEvent(new PostPublished(this.getId(), this.memberId));
    }

    public void hide() {
        if (this.status == PostStatus.HIDDEN) {
            throw new InvalidPostStatusTransitionException(this.status, "숨김");
        }
        if (this.status == PostStatus.DELETED) {
            throw new InvalidPostStatusTransitionException(this.status, "숨김");
        }

        PostStatus previousStatus = this.status;
        this.status = PostStatus.HIDDEN;

        // 도메인 이벤트 발행
        this.addDomainEvent(new PostHidden(this.getId(), this.memberId, previousStatus));
    }

    public void delete() {
        if (this.status == PostStatus.DELETED) {
            throw new InvalidPostStatusTransitionException(this.status, "삭제");
        }

        PostStatus previousStatus = this.status;
        this.status = PostStatus.DELETED;

        // 도메인 이벤트 발행
        this.addDomainEvent(new PostDeleted(this.getId(), this.memberId, previousStatus));
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }
}