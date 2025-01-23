package com.ecommerce.user_service.exception;

import com.ecommerce.user_service.dto.ResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errorMap = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.status(status.value()).body(errorMap);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        ResponseDTO responseDTO = ResponseDTO.builder()
                .errorMessage("An unexpected error occurred: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        ResponseDTO responseDTO = ResponseDTO.builder()
                .errorMessage("An unexpected error occurred at runtime: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        // Collect all validation error messages for multiple fields
        String errorMessages = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", ")); // Join the errors in a single string, separated by commas

        ResponseDTO errorResponse = ResponseDTO.builder()
                .errorMessage("Validation failed: " + errorMessages)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // Handle cases like unique constraint violation
        String msg = ex.getMostSpecificCause().getMessage();
        ResponseDTO errorResponse;
        if (msg.contains("Duplicate entry")) {
            errorResponse = ResponseDTO.builder()
                    .message("Data integrity violation: " + "Email already exists!")
                    .build();
        } else {
            errorResponse = ResponseDTO.builder()
                    .message("Data integrity violation: " + ex.getMostSpecificCause().getMessage())
                    .build();
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ResponseDTO errorResponseDTO = ResponseDTO.builder()
                .errorMessage(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.valueOf(401))
                .body(ResponseDTO.builder()
                        .message("Authentication Failed!")
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.valueOf(403))
                .body(ResponseDTO.builder()
                        .message("Access Denied!")
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignatureException(SignatureException e) {
        return ResponseEntity.status(HttpStatus.valueOf(404))
                .body(ResponseDTO.builder()
                        .message("Invalid JWT!")
                        .errorMessage(e.getMessage())
                        .build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException e) {
        return ResponseEntity.status(HttpStatus.valueOf(404))
                .body(ResponseDTO.builder()
                        .message("JWT Expired!")
                        .errorMessage(e.getMessage())
                        .build());
    }

}
