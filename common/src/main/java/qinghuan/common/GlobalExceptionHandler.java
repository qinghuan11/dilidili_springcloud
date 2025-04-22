package qinghuan.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<?>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "Authentication failed: " + e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, "File size exceeds the maximum limit"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "Internal server error: " + e.getMessage()));
    }
}
