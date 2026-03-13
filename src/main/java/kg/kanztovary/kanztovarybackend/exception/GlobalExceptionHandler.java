package kg.kanztovary.kanztovarybackend.exception;

import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<ResponseDataDto> handleException(ResponseException ex) {
        log.error("ResponseException - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getResponseDataDto());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDataDto> handleDataIntegrityException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseDataDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDataDto> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("ValidationException - {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseDataDto.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDataDto> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ResponseDataDto.builder()
                        .code(HttpStatus.FORBIDDEN.value())
                        .message("Доступ запрещён")
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseDataDto> handleAuthenticationException(AuthenticationException ex) {
        log.error("AuthenticationException - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ResponseDataDto.builder()
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .message("Необходима авторизация")
                        .build()
        );
    }
}
