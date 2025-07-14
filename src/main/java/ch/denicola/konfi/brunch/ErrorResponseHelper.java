package ch.denicola.konfi.brunch;

import ch.denic0la.openapi.konfi.brunch.model.ErrorResponseDTO;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface ErrorResponseHelper {
    public static ErrorResponseDTO createErrorResponse(int status, @NonNull String message, @Nullable String details) {
        var response = new ErrorResponseDTO();
        response.setCode(status);
        response.setMessage(message);
        if (details != null) {
            response.setDetails(JsonNullable.of(details));
        }
        return response;
    }
    public static ErrorResponseDTO createErrorResponse(int status, @NonNull String message) {
        return createErrorResponse(status, message, null);
    }
}
