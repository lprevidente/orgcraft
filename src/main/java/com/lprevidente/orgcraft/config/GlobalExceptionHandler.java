package com.lprevidente.orgcraft.config;

import com.lprevidente.orgcraft.common.exception.DomainException;
import jakarta.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  ProblemDetail handleNoSuchElementException(NoSuchElementException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    return problemDetail;
  }

  @ExceptionHandler(AccessDeniedException.class)
  ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
    final var problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problemDetail.setTitle("Forbidden");
    return problemDetail;
  }

  @ExceptionHandler(DomainException.class)
  ProblemDetail handleDomainException(DomainException ex) {
    final var problemDetail = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    problemDetail.setTitle(ex.getTitle());
    problemDetail.setProperty("errorCode", ex.getErrorCode());
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Invalid Request");
    return problemDetail;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed JSON request");
    problemDetail.setTitle("Invalid JSON");
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setTitle("Validation Error");

    // Add validation errors as properties
    problemDetail.setProperty(
        "errors",
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
            .toList());

    return problemDetail;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint violation");
    problemDetail.setTitle("Validation Error");

    // Add validation errors as properties
    problemDetail.setProperty(
        "errors",
        ex.getConstraintViolations().stream()
            .map(
                violation -> {
                  final var propertyPath = violation.getPropertyPath().toString();
                  final var field =
                      propertyPath.contains(".")
                          ? propertyPath.substring(propertyPath.lastIndexOf(".") + 1)
                          : propertyPath;
                  return new ValidationError(field, violation.getMessage());
                })
            .toList());

    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Failed to convert value '%s' to required type".formatted(ex.getValue()));
    problemDetail.setTitle("Type Mismatch");
    problemDetail.setProperty("parameter", ex.getName());
    problemDetail.setProperty("requiredType", ex.getRequiredType().getSimpleName());
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleGenericException(Exception ex) {
    log.warn("Unexpected exception", ex);
    final var problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problemDetail.setTitle("Internal Server Error");
    return problemDetail;
  }

  /** Record for validation errors */
  record ValidationError(String field, String message) {}
}
