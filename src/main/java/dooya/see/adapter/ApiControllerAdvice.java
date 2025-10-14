package dooya.see.adapter;

import dooya.see.domain.member.exception.AuthenticateException;
import dooya.see.domain.member.exception.DuplicateEmailException;
import dooya.see.domain.member.exception.DuplicateProfileException;
import dooya.see.domain.member.exception.MemberNotFoundException;
import dooya.see.domain.post.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

/**
 * 모든 컨트롤러에서 발생하는 예외를 처리하는 클래스.
 * 예외 유형에 따라 적절한 HTTP 응답 상태와 문제 세부 정보를 반환합니다.
 */
@ControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handlerException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicateProfileException.class, InvalidPostStatusTransitionException.class, InvalidCommentStatusException.class})
    public ProblemDetail conflictExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(AuthenticateException.class)
    public ProblemDetail unauthorizedExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler({EmptyCommentUpdateException.class, ConstraintViolationException.class})
    public ProblemDetail badRequestExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({UnauthorizedPostAccessException.class, UnauthorizedCommentAccessException.class})
    public ProblemDetail forbiddenExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler({MemberNotFoundException.class, PostNotFoundException.class, CommentNotFoundException.class})
    public ProblemDetail notFoundExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.NOT_FOUND, exception);
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
