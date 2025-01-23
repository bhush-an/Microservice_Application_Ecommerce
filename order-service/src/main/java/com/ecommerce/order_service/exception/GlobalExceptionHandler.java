package com.ecommerce.order_service.exception;

import com.ecommerce.order_service.dto.ResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
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
        ResponseDTO errorResponse = ResponseDTO.builder()
                .errorMessage("Data integrity violation: " + ex.getMostSpecificCause().getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleOrderNotFoundException(OrderNotFoundException ex) {
        ResponseDTO errorResponse = ResponseDTO.builder()
                .errorMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleProductNotFoundException(ProductNotFoundException e) {
        ResponseDTO response = ResponseDTO.builder()
                .errorMessage(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientQuantityException.class)
    public ResponseEntity<ResponseDTO> handleInsufficientQuantityException(InsufficientQuantityException e) {
        ResponseDTO response = ResponseDTO.builder()
                .errorMessage(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingCustomerException.class)
    public ResponseEntity<ResponseDTO> handleMissingCustomerException(MissingCustomerException e) {
        ResponseDTO response = ResponseDTO.builder()
                .errorMessage(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleCustomerNotFoundException(CustomerNotFoundException e) {
        ResponseDTO response = ResponseDTO.builder()
                .errorMessage(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceNotAvailableException.class)
    public ResponseEntity<ResponseDTO> handleServiceNotAvailableException(ServiceNotAvailableException ex) {
        ResponseDTO errorResponse = ResponseDTO.builder()
                .errorCode(503)
                .service(ex.getMessage())
                .errorMessage("Service Unavailable, please try again later!")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
