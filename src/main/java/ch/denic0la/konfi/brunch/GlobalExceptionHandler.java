package ch.denic0la.konfi.brunch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.java.Log;

@ControllerAdvice
@Log
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex, WebRequest request) {
    log.warning("ResponseStatusException: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(ex.getStatusCode().value())
            .error(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase())
            .message(ex.getReason())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, ex.getStatusCode());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    log.warning("Validation error: " + ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message("Validation failed")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(errors)
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    log.warning("Constraint violation: " + ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      errors.put(propertyPath, message);
    }

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message("Validation failed")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(errors)
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    log.warning("Illegal argument: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException ex, WebRequest request) {
    log.warning("Bad credentials: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message("Invalid credentials")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {
    log.warning("Authentication error: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message("Authentication failed")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {
    log.warning("Access denied: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("Access denied")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  @ExceptionHandler({NoHandlerFoundException.class})
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
      NoHandlerFoundException ex, WebRequest request) {
    log.warning("Not implemented: " + ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.NOT_IMPLEMENTED.value())
            .error("Not Implemented")
            .message("Support for this endpoint is not implemented")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
    log.severe("Unexpected error: " + ex.getMessage());
    ex.printStackTrace(); // Log full stack trace for debugging

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  public static class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private long timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse() {
      this.timestamp = System.currentTimeMillis();
    }

    public static ErrorResponseBuilder builder() {
      return new ErrorResponseBuilder();
    }

    // Getters and setters
    public int getStatus() {
      return status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public Map<String, String> getValidationErrors() {
      return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
      this.validationErrors = validationErrors;
    }

    public static class ErrorResponseBuilder {
      private final ErrorResponse errorResponse;

      public ErrorResponseBuilder() {
        this.errorResponse = new ErrorResponse();
      }

      public ErrorResponseBuilder status(int status) {
        errorResponse.setStatus(status);
        return this;
      }

      public ErrorResponseBuilder error(String error) {
        errorResponse.setError(error);
        return this;
      }

      public ErrorResponseBuilder message(String message) {
        errorResponse.setMessage(message);
        return this;
      }

      public ErrorResponseBuilder path(String path) {
        errorResponse.setPath(path);
        return this;
      }

      public ErrorResponseBuilder validationErrors(Map<String, String> validationErrors) {
        errorResponse.setValidationErrors(validationErrors);
        return this;
      }

      public ErrorResponse build() {
        return errorResponse;
      }
    }
  }
}
