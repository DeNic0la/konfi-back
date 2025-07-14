package ch.denicola.konfi.brunch;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import ch.denic0la.openapi.konfi.brunch.model.ErrorResponseDTO;

public interface ErrorResponseHelper {
  static ErrorResponseDTO createErrorResponse(
      int status, @NonNull String message, @Nullable String details) {
    var response = new ErrorResponseDTO();
    response.setCode(status);
    response.setMessage(message);
    if (details != null) {
      response.setDetails(JsonNullable.of(details));
    }
    return response;
  }

  static ErrorResponseDTO createErrorResponse(int status, @NonNull String message) {
    return createErrorResponse(status, message, null);
  }
}
