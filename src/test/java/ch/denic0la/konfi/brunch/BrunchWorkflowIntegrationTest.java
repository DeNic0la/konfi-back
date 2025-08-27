package ch.denic0la.konfi.brunch;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"test"})
@Transactional
class BrunchWorkflowIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private BrunchRepository brunchRepository;

  @Autowired private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @Test
  @DisplayName("Complete brunch creation and retrieval workflow")
  void completeWorkflowTest() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Step 1: Verify no brunches exist initially
    mockMvc
        .perform(get("/api/brunches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    // Step 2: Create a comprehensive brunch with questions
    BrunchCreateDTO brunchCreateDTO = createComprehensiveBrunchDTO();

    String createResponse =
        mockMvc
            .perform(
                post("/api/brunches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(brunchCreateDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("integration-test-brunch"))
            .andExpect(jsonPath("$.title").value("Integration Test Brunch"))
            .andExpect(jsonPath("$.requireEmail").value(true))
            .andExpect(jsonPath("$.emailRegexp").value("/.*@company\\.com_/"))
            .andExpect(jsonPath("$.questions.length()").value(3))
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Step 3: Verify the brunch appears in the list
    mockMvc
        .perform(get("/api/brunches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0]").value("integration-test-brunch"));

    // Step 4: Retrieve the created brunch by ID
    mockMvc
        .perform(get("/api/brunches/integration-test-brunch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("integration-test-brunch"))
        .andExpect(jsonPath("$.title").value("Integration Test Brunch"))
        .andExpect(jsonPath("$.requireEmail").value(true))
        .andExpect(jsonPath("$.questions.length()").value(3));

    // Step 5: Verify the brunch was actually persisted in the database
    assertThat(brunchRepository.existsById("integration-test-brunch")).isTrue();
    var savedBrunch = brunchRepository.findById("integration-test-brunch");
    assertThat(savedBrunch).isPresent();
    assertThat(savedBrunch.get().getTitle()).isEqualTo("Integration Test Brunch");
    assertThat(savedBrunch.get().getQuestions()).hasSize(3);

    // Verify questions are ordered correctly
    var questions = savedBrunch.get().getQuestions();
    assertThat(questions.get(0).getTitle()).isEqualTo("First Question");
    assertThat(questions.get(1).getTitle()).isEqualTo("Second Question");
    assertThat(questions.get(2).getTitle()).isEqualTo("Third Question");
  }

  @Test
  @DisplayName("Error handling workflow - duplicate brunch creation")
  void duplicateBrunchCreationWorkflow() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    BrunchCreateDTO brunchDTO = createSimpleBrunchDTO("duplicate-test", "Duplicate Test");

    // Step 1: Create the first brunch successfully
    mockMvc
        .perform(
            post("/api/brunches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(brunchDTO)))
        .andExpect(status().isCreated());

    // Step 2: Attempt to create the same brunch again - should fail
    mockMvc
        .perform(
            post("/api/brunches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(brunchDTO)))
        .andExpect(status().isConflict());

    // Step 3: Verify only one brunch exists
    mockMvc
        .perform(get("/api/brunches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  @DisplayName("Multiple brunches workflow")
  void multipleBrunchesWorkflow() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Create multiple brunches
    BrunchCreateDTO brunch1 = createSimpleBrunchDTO("multi-test-1", "First Brunch");
    BrunchCreateDTO brunch2 = createSimpleBrunchDTO("multi-test-2", "Second Brunch");
    BrunchCreateDTO brunch3 = createSimpleBrunchDTO("multi-test-3", "Third Brunch");

    // Create all brunches
    for (BrunchCreateDTO dto : List.of(brunch1, brunch2, brunch3)) {
      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isCreated());
    }

    // Verify all brunches are listed
    mockMvc
        .perform(get("/api/brunches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));

    // Verify each brunch can be retrieved individually
    for (String id : List.of("multi-test-1", "multi-test-2", "multi-test-3")) {
      mockMvc
          .perform(get("/api/brunches/" + id))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id));
    }
  }

  @Test
  @DisplayName("Non-existent brunch retrieval workflow")
  void nonExistentBrunchWorkflow() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // Attempt to retrieve a non-existent brunch
    mockMvc.perform(get("/api/brunches/does-not-exist")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Brunch with complex email validation workflow")
  void emailValidationWorkflow() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    BrunchCreateDTO emailBrunch = new BrunchCreateDTO();
    emailBrunch.setId("email-validation-test");
    emailBrunch.setTitle("Email Validation Test");
    emailBrunch.setRequireEmail(true);
    emailBrunch.setEmailRegexp(JsonNullable.of("/^[a-zA-Z0-9._%+-]+@company\\.(com|org)$_/i"));
    emailBrunch.setAdminPassword(JsonNullable.of("admin123"));
    emailBrunch.setVotingPassword(JsonNullable.of("vote123"));
    emailBrunch.setQuestions(Set.of());

    // Create brunch with email validation
    mockMvc
        .perform(
            post("/api/brunches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailBrunch)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.requireEmail").value(true))
        .andExpect(jsonPath("$.emailRegexp").value("/^[a-zA-Z0-9._%+-]+@company\\.(com|org)$_/i"));

    // Verify the email regexp was correctly saved
    var savedBrunch = brunchRepository.findById("email-validation-test");
    assertThat(savedBrunch).isPresent();
    assertThat(savedBrunch.get().getRequireEmail()).isTrue();
    assertThat(savedBrunch.get().getEmailRegexp())
        .isEqualTo("/^[a-zA-Z0-9._%+-]+@company\\.(com|org)$_/i");
  }

  private BrunchCreateDTO createComprehensiveBrunchDTO() {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId("integration-test-brunch");
    dto.setTitle("Integration Test Brunch");
    dto.setRequireEmail(true);
    dto.setEmailRegexp(JsonNullable.of("/.*@company\\.com_/"));
    dto.setAdminPassword(JsonNullable.of("admin-password-123"));
    dto.setVotingPassword(JsonNullable.of("voting-password-456"));

    // Create three questions with different configurations
    BrunchQuestionDTO question1 = new BrunchQuestionDTO();
    question1.setId(null); // Let JPA auto-generate the ID
    question1.setTitle(JsonNullable.of("First Question"));
    question1.setMin(1);
    question1.setMax(5);
    question1.setOptional(false);
    question1.setOrder(JsonNullable.of(1));
    question1.setRecommended(JsonNullable.of(3));
    question1.setLink(JsonNullable.of(URI.create("https://example.com/q1")));

    BrunchQuestionDTO question2 = new BrunchQuestionDTO();
    question2.setId(null); // Let JPA auto-generate the ID
    question2.setTitle(JsonNullable.of("Second Question"));
    question2.setMin(0);
    question2.setMax(10);
    question2.setOptional(true);
    question2.setOrder(JsonNullable.of(2));
    question2.setRecommended(JsonNullable.of(7));

    BrunchQuestionDTO question3 = new BrunchQuestionDTO();
    question3.setId(null); // Let JPA auto-generate the ID
    question3.setTitle(JsonNullable.of("Third Question"));
    question3.setMin(1);
    question3.setMax(3);
    question3.setOptional(false);
    question3.setOrder(JsonNullable.of(3));
    question3.setLink(JsonNullable.of(URI.create("https://example.com/q3")));

    dto.setQuestions(Set.of(question1, question2, question3));
    return dto;
  }

  private BrunchCreateDTO createSimpleBrunchDTO(String id, String title) {
    BrunchCreateDTO dto = new BrunchCreateDTO();
    dto.setId(id);
    dto.setTitle(title);
    dto.setRequireEmail(false);
    dto.setEmailRegexp(JsonNullable.undefined());
    dto.setAdminPassword(JsonNullable.of("admin"));
    dto.setVotingPassword(JsonNullable.of("vote"));
    dto.setQuestions(Set.of());
    return dto;
  }
}
