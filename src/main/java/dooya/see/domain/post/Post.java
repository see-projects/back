package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.shared.AbstractAggregateRoot;
import jakarta.persistence.*;
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

    @Transient
    private PostCreationContext creationContext;

    private record PostCreationContext(boolean publishImmediately) {}

    public static Post create(PostCreateRequest request, Long memberId) {
        Post post = new Post();

        post.content = new PostContent(request.title(), request.body());
        post.memberId = requireNonNull(memberId);
        post.category = requireNonNull(request.category());
        post.status = request.publishImmediately() ? PostStatus.PUBLISHED : PostStatus.DRAFT;
        post.metaData = request.publishImmediately() ? PostMetaData.createPublished() : PostMetaData.create();

        post.creationContext = new PostCreationContext(request.publishImmediately());

        return post;
    }

    public void publishCreationEventIfNeeded() {
        publishCreationEvent();
    }

    private void publishCreationEvent() {
        if (creationContext != null && getId() != null) {
            addDomainEvent(new PostCreated(
                    getId(), memberId, category, creationContext.publishImmediately()
            ));
            creationContext = null;
        }
    }

    public void update(PostUpdateRequest request) {
        state(request.hasAnyUpdate(), "변경사항이 없습니다");

        boolean titleChanged = false;
        boolean bodyChanged = false;
        boolean categoryChanged = false;

        if (hasContentToUpdate(request)) {
            String originalTitle = this.content.title();
            String originalBody = this.content.body();
            
            String newTitle = request.title().orElse(originalTitle);
            String newBody = request.body().orElse(originalBody);

            titleChanged = !originalTitle.equals(newTitle);
            bodyChanged = !originalBody.equals(newBody);

            this.content = new PostContent(newTitle, newBody);
        }

        if (hasCategoryToUpdate(request)) {
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

    private static boolean hasCategoryToUpdate(PostUpdateRequest request) {
        return request.category().isPresent();
    }

    private static boolean hasContentToUpdate(PostUpdateRequest request) {
        return request.title().isPresent() || request.body().isPresent();
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

    public void view(Long viewerId) {
        // 조회수 증가는 PostStats에서 처리하고, 여기서는 이벤트만 발행
        if (this.getId() != null) {
            this.addDomainEvent(new PostViewed(this.getId(), viewerId));
        }
    }

    public void publishLikeEvent(Long memberId) {
        requireNonNull(memberId, "좋아요를 누를 회원 ID는 필수입니다");
        
        if (this.getId() != null) {
            this.addDomainEvent(new PostLiked(this.getId(), memberId));
        }
    }

    public void publishUnlikeEvent(Long memberId) {
        requireNonNull(memberId, "좋아요를 취소할 회원 ID는 필수입니다");
        
        if (this.getId() != null) {
            this.addDomainEvent(new PostUnliked(this.getId(), memberId));
        }
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }
}