package dooya.see.domain.post.exception;

import dooya.see.domain.post.PostStatus;

public class InvalidPostStatusTransitionException extends RuntimeException {
    private final PostStatus currentStatus;
    private final String attemptedAction;
    
    public InvalidPostStatusTransitionException(PostStatus currentStatus, String attemptedAction) {
        super(String.format("현재 상태 '%s'에서 '%s' 작업을 수행할 수 없습니다", 
                           currentStatus, attemptedAction));
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }
    
    public PostStatus getCurrentStatus() { 
        return currentStatus; 
    }
    
    public String getAttemptedAction() { 
        return attemptedAction; 
    }
}
