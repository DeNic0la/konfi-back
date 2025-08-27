package ch.denic0la.konfi.util;

import java.util.Set;

import org.openapitools.jackson.nullable.JsonNullable;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;

/**
 * Utility class for creating test DTOs with JsonNullable fields.
 * This helps avoid JsonNullable serialization issues in unit tests
 * by providing pre-configured test objects.
 */
public final class JsonNullableTestUtils {

  private JsonNullableTestUtils() {}

  /**
   * Creates a minimal BrunchCreateDTO for testing without JsonNullable complexity
   */
  public static BrunchCreateDTO createMinimalBrunchCreateDTO() {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId("test-brunch");
    dto.setTitle("Test Brunch");
    dto.setRequireEmail(false);
    dto.setQuestions(Set.of());
    return dto;
  }

  /**
   * Creates a full BrunchCreateDTO with all JsonNullable fields set
   */
  public static BrunchCreateDTO createFullBrunchCreateDTO() {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId("full-test-brunch");
    dto.setTitle("Full Test Brunch");
    dto.setRequireEmail(true);
    dto.setEmailRegexp(JsonNullable.of(".*@test\\.com"));
    dto.setAdminPassword(JsonNullable.of("admin123"));
    dto.setVotingPassword(JsonNullable.of("vote123"));

    BrunchQuestionDTO questionDTO = createBrunchQuestionDTO();
    dto.setQuestions(Set.of(questionDTO));

    return dto;
  }

  /**
   * Creates a BrunchCreateDTO with undefined JsonNullable fields
   */
  public static BrunchCreateDTO createBrunchCreateDTOWithUndefinedFields() {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId("undefined-test-brunch");
    dto.setTitle("Undefined Test Brunch");
    dto.setRequireEmail(false);
    dto.setEmailRegexp(JsonNullable.undefined());
    dto.setAdminPassword(JsonNullable.undefined());
    dto.setVotingPassword(JsonNullable.undefined());
    dto.setQuestions(Set.of());
    return dto;
  }

  /**
   * Creates a BrunchCreateDTO with null JsonNullable fields
   */
  public static BrunchCreateDTO createBrunchCreateDTOWithNullFields() {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId("null-test-brunch");
    dto.setTitle("Null Test Brunch");
    dto.setRequireEmail(false);
    dto.setEmailRegexp(JsonNullable.of(null));
    dto.setAdminPassword(JsonNullable.of(null));
    dto.setVotingPassword(JsonNullable.of(null));
    dto.setQuestions(Set.of());
    return dto;
  }

  /**
   * Creates a minimal BrunchInfoDTO for testing
   */
  public static BrunchInfoDTO createMinimalBrunchInfoDTO() {
    BrunchInfoDTO dto = new BrunchInfoDTO();
    dto.setId("test-brunch-info");
    dto.setTitle("Test Brunch Info");
    dto.setRequireEmail(false);
    dto.setQuestions(Set.of());
    return dto;
  }

  /**
   * Creates a full BrunchInfoDTO with all JsonNullable fields set
   */
  public static BrunchInfoDTO createFullBrunchInfoDTO() {
    BrunchInfoDTO dto = new BrunchInfoDTO();
    dto.setId("full-test-brunch-info");
    dto.setTitle("Full Test Brunch Info");
    dto.setRequireEmail(true);
    dto.setEmailRegexp(JsonNullable.of(".*@test\\.com"));

    BrunchQuestionInfoDTO questionInfoDTO = createBrunchQuestionInfoDTO();
    dto.setQuestions(Set.of(questionInfoDTO));

    return dto;
  }

  /**
   * Creates a BrunchQuestionDTO for testing
   */
  public static BrunchQuestionDTO createBrunchQuestionDTO() {
    BrunchQuestionDTO dto = new BrunchQuestionDTO();
    dto.setId(1);
    dto.setTitle(JsonNullable.of("Test Question"));
    dto.setMin(1);
    dto.setMax(5);
    dto.setOptional(false);
    dto.setOrder(JsonNullable.of(1));
    dto.setRecommended(JsonNullable.of(3));
    return dto;
  }

  /**
   * Creates a BrunchQuestionInfoDTO for testing
   */
  public static BrunchQuestionInfoDTO createBrunchQuestionInfoDTO() {
    BrunchQuestionInfoDTO dto = new BrunchQuestionInfoDTO();
    dto.setId(1);
    dto.setTitle(JsonNullable.of("Test Question Info"));
    dto.setMin(1);
    dto.setMax(5);
    dto.setOptional(false);
    dto.setOrder(1);
    dto.setRecommended(3);
    return dto;
  }

  /**
   * Creates a BrunchQuestionDTO with undefined JsonNullable fields
   */
  public static BrunchQuestionDTO createBrunchQuestionDTOWithUndefinedFields() {
    BrunchQuestionDTO dto = new BrunchQuestionDTO();
    dto.setId(1);
    dto.setTitle(JsonNullable.undefined());
    dto.setMin(1);
    dto.setMax(5);
    dto.setOptional(false);
    dto.setOrder(JsonNullable.undefined());
    dto.setRecommended(JsonNullable.undefined());
    return dto;
  }

  /**
   * Checks if a JsonNullable field is properly set
   */
  public static boolean isJsonNullablePresent(JsonNullable<?> field) {
    return field != null && field.isPresent();
  }

  /**
   * Checks if a JsonNullable field is undefined
   */
  public static boolean isJsonNullableUndefined(JsonNullable<?> field) {
    return field == null || !field.isPresent();
  }

  /**
   * Safely gets the value from a JsonNullable field
   */
  public static <T> T getJsonNullableValue(JsonNullable<T> field) {
    return (field != null && field.isPresent()) ? field.get() : null;
  }
}
