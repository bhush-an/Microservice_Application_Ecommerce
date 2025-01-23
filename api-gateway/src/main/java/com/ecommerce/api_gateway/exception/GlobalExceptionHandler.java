package com.ecommerce.api_gateway.exception;

import com.ecommerce.api_gateway.dto.ResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

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
