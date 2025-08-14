package dooya.see.domain.member;

public class DuplicateProfileException extends RuntimeException {
    public DuplicateProfileException(String message) {
        super(message);
    }
}
