package dooya.see.adapter;

import dooya.see.domain.member.AuthenticateException;
import dooya.see.domain.member.DuplicateEmailException;
import dooya.see.domain.member.DuplicateProfileException;
import dooya.see.domain.member.MemberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handlerException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicateProfileException.class})
    public ProblemDetail conflictExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(AuthenticateException.class)
    public ProblemDetail unauthorizedExceptionHandler(Exception exception) {
        return getProblemDetail(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler(MemberNotFoundException.class)
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
