package ch.denic0la.konfi.brunch;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.denic0la.konfi.brunch.data.BrunchRepository;
import ch.denic0la.konfi.config.TestWebConfig;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestWebConfig.class})
@AutoConfigureWebMvc
@ActiveProfiles({"test"})
@Transactional
class BrunchIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private BrunchRepository brunchRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    // Clean up database before each test
    brunchRepository.deleteAll();
  }

  @Nested
  @DisplayName("Full integration tests")
  class FullIntegrationTests {

    @Test
    @DisplayName("Should create and retrieve brunch with JsonNullable fields")
    void shouldCreateAndRetrieveBrunchWithJsonNullableFields() throws Exception {
      // Create brunch with JsonNullable fields
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("integration-test-brunch");
      createDTO.setTitle("Integration Test Brunch");
      createDTO.setRequireEmail(true);
      createDTO.setEmailRegexp(JsonNullable.undefined());
      createDTO.setAdminPassword(JsonNullable.of("admin-integration"));
      createDTO.setVotingPassword(JsonNullable.of("vote-integration"));

      BrunchQuestionDTO questionDTO = new BrunchQuestionDTO();
      questionDTO.setId(1);
      questionDTO.setTitle(JsonNullable.of("Integration Question"));
      questionDTO.setMin(1);
      questionDTO.setMax(10);
      questionDTO.setOptional(false);
      questionDTO.setOrder(JsonNullable.of(1));
      questionDTO.setRecommended(JsonNullable.of(5));

      createDTO.setQuestions(Set.of(questionDTO));

      // Create brunch via POST
      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value("integration-test-brunch"))
          .andExpect(jsonPath("$.title").value("Integration Test Brunch"))
          .andExpect(jsonPath("$.requireEmail").value(true))
          .andExpect(jsonPath("$.questions").isArray())
          .andExpect(jsonPath("$.questions[0].title").value("Integration Question"))
          .andExpect(jsonPath("$.questions[0].min").value(1))
          .andExpect(jsonPath("$.questions[0].max").value(10))
          .andExpect(jsonPath("$.questions[0].recommended").value(5));

      // Verify brunch exists in database
      assertThat(brunchRepository.existsById("integration-test-brunch")).isTrue();

      // Retrieve brunch via GET
      mockMvc
          .perform(get("/api/brunches/integration-test-brunch"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value("integration-test-brunch"))
          .andExpect(jsonPath("$.title").value("Integration Test Brunch"))
          .andExpect(jsonPath("$.requireEmail").value(true))
          .andExpect(jsonPath("$.questions").isArray())
          .andExpect(jsonPath("$.questions[0].title").value("Integration Question"));
    }

    @Test
    @DisplayName("Should handle brunch with undefined JsonNullable fields")
    void shouldHandleBrunchWithUndefinedFields() throws Exception {
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("minimal-brunch");
      createDTO.setTitle("Minimal Brunch");
      createDTO.setRequireEmail(false);
      // Leave JsonNullable fields as undefined
      createDTO.setEmailRegexp(JsonNullable.undefined());
      createDTO.setAdminPassword(JsonNullable.undefined());
      createDTO.setVotingPassword(JsonNullable.undefined());
      createDTO.setQuestions(Set.of());

      // Create brunch
      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value("minimal-brunch"))
          .andExpect(jsonPath("$.title").value("Minimal Brunch"))
          .andExpect(jsonPath("$.requireEmail").value(false))
          .andExpect(jsonPath("$.questions").isEmpty());

      // Verify in database
      assertThat(brunchRepository.existsById("minimal-brunch")).isTrue();
    }

    @Test
    @DisplayName("Should handle brunch with null JsonNullable fields")
    void shouldHandleBrunchWithNullFields() throws Exception {
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("null-fields-brunch");
      createDTO.setTitle("Null Fields Brunch");
      createDTO.setRequireEmail(false);
      // Set JsonNullable fields to explicit null
      createDTO.setEmailRegexp(JsonNullable.of(null));
      createDTO.setAdminPassword(JsonNullable.of(null));
      createDTO.setVotingPassword(JsonNullable.of(null));
      createDTO.setQuestions(Set.of());

      // Create brunch
      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value("null-fields-brunch"))
          .andExpect(jsonPath("$.title").value("Null Fields Brunch"))
          .andExpect(jsonPath("$.requireEmail").value(false));

      // Verify in database
      assertThat(brunchRepository.existsById("null-fields-brunch")).isTrue();
    }

    @Test
    @DisplayName("Should return 409 for duplicate brunch ID")
    void shouldReturn409ForDuplicateBrunchId() throws Exception {
      // First, create a brunch
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("duplicate-test");
      createDTO.setTitle("First Brunch");
      createDTO.setRequireEmail(false);
      createDTO.setQuestions(Set.of());

      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createDTO)))
          .andExpect(status().isCreated());

      // Try to create another brunch with same ID
      BrunchCreateDTO duplicateDTO = new BrunchCreateDTO();
      duplicateDTO.setId("duplicate-test");
      duplicateDTO.setTitle("Second Brunch");
      duplicateDTO.setRequireEmail(false);
      duplicateDTO.setQuestions(Set.of());

      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(duplicateDTO)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 404 for non-existent brunch")
    void shouldReturn404ForNonExistentBrunch() throws Exception {
      mockMvc.perform(get("/api/brunches/non-existent-brunch")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should list all brunch IDs")
    void shouldListAllBrunchIds() throws Exception {
      // Create multiple brunches
      for (int i = 1; i <= 3; i++) {
        BrunchCreateDTO createDTO = new BrunchCreateDTO();
        createDTO.setId("brunch-" + i);
        createDTO.setTitle("Brunch " + i);
        createDTO.setRequireEmail(false);
        createDTO.setQuestions(Set.of());

        mockMvc
            .perform(
                post("/api/brunches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
            .andExpect(status().isCreated());
      }

      // Get all brunches
      mockMvc
          .perform(get("/api/brunches"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(3))
          .andExpect(
              jsonPath("$[*]")
                  .value(
                      org.hamcrest.Matchers.containsInAnyOrder(
                          "brunch-1", "brunch-2", "brunch-3")));
    }
  }

  @Nested
  @DisplayName("JsonNullable serialization tests")
  class JsonNullableSerializationTests {

    @Test
    @DisplayName("Should properly serialize and deserialize JsonNullable values")
    void shouldSerializeAndDeserializeJsonNullableValues() throws Exception {
      BrunchCreateDTO originalDTO = new BrunchCreateDTO();
      originalDTO.setId("serialization-test");
      originalDTO.setTitle("Serialization Test");
      originalDTO.setRequireEmail(true);
      originalDTO.setEmailRegexp(JsonNullable.of("/test_pattern/"));
      originalDTO.setAdminPassword(JsonNullable.of("admin"));
      originalDTO.setVotingPassword(JsonNullable.of("vote"));

      BrunchQuestionDTO questionDTO = new BrunchQuestionDTO();
      questionDTO.setId(1);
      questionDTO.setTitle(JsonNullable.of("Test Question"));
      questionDTO.setMin(1);
      questionDTO.setMax(5);
      questionDTO.setOptional(true);
      questionDTO.setOrder(JsonNullable.of(1));
      questionDTO.setRecommended(JsonNullable.of(3));

      originalDTO.setQuestions(Set.of(questionDTO));

      // Serialize to JSON
      String json = objectMapper.writeValueAsString(originalDTO);
      assertThat(json).isNotNull();
      assertThat(json).contains("\"emailRegexp\":\"/test_pattern/\"");
      assertThat(json).contains("\"title\":\"Test Question\"");

      // Deserialize from JSON
      BrunchCreateDTO deserializedDTO = objectMapper.readValue(json, BrunchCreateDTO.class);
      assertThat(deserializedDTO).isNotNull();
      assertThat(deserializedDTO.getId()).isEqualTo("serialization-test");
      assertThat(deserializedDTO.getEmailRegexp().isPresent()).isTrue();
      assertThat(deserializedDTO.getEmailRegexp().get()).isEqualTo("/test_pattern/");

      BrunchQuestionDTO deserializedQuestion = deserializedDTO.getQuestions().iterator().next();
      assertThat(deserializedQuestion.getTitle().isPresent()).isTrue();
      assertThat(deserializedQuestion.getTitle().get()).isEqualTo("Test Question");
      assertThat(deserializedQuestion.getRecommended().isPresent()).isTrue();
      assertThat(deserializedQuestion.getRecommended().get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle undefined JsonNullable fields in JSON")
    void shouldHandleUndefinedJsonNullableFields() throws Exception {
      // JSON with some fields missing (undefined)
      String jsonWithMissingFields =
          """
          {
            "id": "undefined-test",
            "title": "Undefined Test",
            "requireEmail": false,
            "questions": []
          }
          """;

      BrunchCreateDTO deserializedDTO =
          objectMapper.readValue(jsonWithMissingFields, BrunchCreateDTO.class);
      assertThat(deserializedDTO).isNotNull();
      assertThat(deserializedDTO.getId()).isEqualTo("undefined-test");
      assertThat(deserializedDTO.getEmailRegexp().isPresent()).isFalse();
      assertThat(deserializedDTO.getAdminPassword().isPresent()).isFalse();
      assertThat(deserializedDTO.getVotingPassword().isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should handle null JsonNullable fields in JSON")
    void shouldHandleNullJsonNullableFields() throws Exception {
      // JSON with explicit null values
      String jsonWithNullFields =
          """
          {
            "id": "null-test",
            "title": "Null Test",
            "requireEmail": false,
            "emailRegexp": null,
            "adminPassword": null,
            "votingPassword": null,
            "questions": []
          }
          """;

      BrunchCreateDTO deserializedDTO =
          objectMapper.readValue(jsonWithNullFields, BrunchCreateDTO.class);
      assertThat(deserializedDTO).isNotNull();
      assertThat(deserializedDTO.getId()).isEqualTo("null-test");
      assertThat(deserializedDTO.getEmailRegexp().isPresent()).isTrue();
      assertThat(deserializedDTO.getEmailRegexp().get()).isNull();
      assertThat(deserializedDTO.getAdminPassword().isPresent()).isTrue();
      assertThat(deserializedDTO.getAdminPassword().get()).isNull();
    }
  }
}
