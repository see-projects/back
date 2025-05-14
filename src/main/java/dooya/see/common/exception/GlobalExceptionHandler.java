package dooya.see.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logException("IllegalArgumentException", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        logException("ConstraintViolationException", ex);
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;
        List<ErrorResponse.ValidationError> validationErrors = extractValidationErrors(ex.getConstraintViolations());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(errorCode, validationErrors));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        logException("CustomException", ex);
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logException("MethodArgumentNotValidException", ex);
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;
        List<ErrorResponse.ValidationError> validationErrors = extractValidationErrors(ex.getBindingResult());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(errorCode, validationErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logException("HttpMessageNotReadableException", ex);
        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;
        String errorMessage = errorCode.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex) {
        logException("UnhandledException", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(errorCode.getMessage()));
    }

    private void logException(String title, Exception ex) {
        log.error("[{}] 예외 발생: {}", title, ex.getMessage(), ex);
    }

    private List<ErrorResponse.ValidationError> extractValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(ErrorResponse.ValidationError::of)
                .collect(Collectors.toList());
    }

    private List<ErrorResponse.ValidationError> extractValidationErrors(Set<ConstraintViolation<?>> violations) {
        return violations
                .stream()
                .map(ErrorResponse.ValidationError::of)
                .collect(Collectors.toList());
    }
}