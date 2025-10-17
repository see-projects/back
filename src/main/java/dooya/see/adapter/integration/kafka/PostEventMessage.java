package dooya.see.adapter.integration.kafka;

import java.io.Serializable;

public record PostEventMessage(PostEventType type, Long postId) implements Serializable {
    static PostEventMessage created(Long postId) {
        return new PostEventMessage(PostEventType.CREATED, postId);
    }

    static PostEventMessage updated(Long postId) {
        return new PostEventMessage(PostEventType.UPDATED, postId);
    }

    static PostEventMessage deleted(Long postId) {
        return new PostEventMessage(PostEventType.DELETED, postId);
    }
}
