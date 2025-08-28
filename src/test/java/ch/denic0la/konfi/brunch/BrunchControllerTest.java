package ch.denic0la.konfi.brunch;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.denic0la.konfi.brunch.data.Brunch;
import ch.denic0la.konfi.brunch.data.BrunchRepository;
import ch.denic0la.konfi.brunch.data.BrunchService;
import ch.denic0la.konfi.brunch.data.Question;
import ch.denic0la.konfi.brunch.data.Vote;
import ch.denic0la.konfi.brunch.data.VoteRepository;
import ch.denic0la.konfi.brunch.security.BrunchPasswordAuthenticationToken;
import ch.denic0la.konfi.config.TestSecurityConfig;
import ch.denic0la.konfi.config.TestWebConfig;
import ch.denic0la.openapi.konfi.brunch.model.BrunchAnswerDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchUpdateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import({TestWebConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
class BrunchControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private BrunchService brunchService;

  @MockBean private BrunchRepository brunchRepository;

  @MockBean private VoteRepository voteRepository;

  private BrunchCreateDTO sampleCreateDTO;
  private BrunchInfoDTO sampleInfoDTO;
  private BrunchUpdateDTO sampleUpdateDTO;
  private BrunchVoteDTO sampleVoteDTO;
  private Brunch sampleBrunch;
  private Vote sampleVote;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    setupSampleData();
    // Clear security context before each test
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("Health check endpoint")
  class HealthCheckTests {

    @Test
    @DisplayName("Should return OK for health check endpoint")
    void shouldReturnOkForHealthCheck() throws Exception {
      mockMvc
          .perform(get("/api/ok"))
          .andExpect(status().isOk())
          .andExpect(content().string("\"OK\""));
    }
  }

  @Nested
  @DisplayName("Create brunch endpoint")
  class CreateBrunchTests {

    @Test
    @DisplayName("Should return 409 when brunch with ID already exists")
    void shouldReturn409WhenBrunchAlreadyExists() throws Exception {
      when(brunchRepository.existsById("existing-brunch")).thenReturn(true);

      BrunchCreateDTO existingBrunchDTO = new BrunchCreateDTO();
      existingBrunchDTO.setId("existing-brunch");
      existingBrunchDTO.setTitle("Existing Brunch");
      existingBrunchDTO.setQuestions(new LinkedHashSet<>());

      mockMvc
          .perform(
              post("/api/brunches")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(existingBrunchDTO)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("Get all brunches endpoint")
  class GetAllBrunchesTests {

    @Test
    @DisplayName("Should return list of all brunch IDs")
    void shouldReturnListOfAllBrunchIds() throws Exception {
      when(brunchRepository.getAllBrunchIds())
          .thenReturn(List.of("brunch-1", "brunch-2", "brunch-3"));

      mockMvc
          .perform(get("/api/brunches"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(3)))
          .andExpect(jsonPath("$[0]", is("brunch-1")))
          .andExpect(jsonPath("$[1]", is("brunch-2")))
          .andExpect(jsonPath("$[2]", is("brunch-3")));
    }

    @Test
    @DisplayName("Should return empty list when no brunches exist")
    void shouldReturnEmptyListWhenNoBrunches() throws Exception {
      when(brunchRepository.getAllBrunchIds()).thenReturn(List.of());

      mockMvc
          .perform(get("/api/brunches"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  @Nested
  @DisplayName("Get brunch by ID endpoint")
  class GetBrunchByIdTests {

    @Test
    @DisplayName("Should return 404 when brunch not found")
    void shouldReturn404WhenBrunchNotFound() throws Exception {
      when(brunchRepository.findById("non-existent")).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/brunches/non-existent")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Delete brunch by ID endpoint")
  class DeleteBrunchByIdTests {

    @Test
    @DisplayName("Should delete brunch successfully with admin authentication")
    void shouldDeleteBrunchSuccessfullyWithAdminAuth() throws Exception {
      setUpAdminAuthentication("test-brunch");
      when(brunchRepository.existsById("test-brunch")).thenReturn(true);
      doNothing().when(brunchRepository).deleteById("test-brunch");

      mockMvc.perform(delete("/api/brunches/test-brunch")).andExpect(status().isNoContent());

      verify(brunchRepository, times(1)).deleteById("test-brunch");
    }

    @Test
    @DisplayName("Should return 403 when not authenticated")
    void shouldReturn403WhenNotAuthenticated() throws Exception {
      mockMvc.perform(delete("/api/brunches/test-brunch")).andExpect(status().isForbidden());

      verify(brunchRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should return 403 when authenticated as voter (not admin)")
    void shouldReturn403WhenAuthenticatedAsVoter() throws Exception {
      setUpVoterAuthentication("test-brunch");

      mockMvc.perform(delete("/api/brunches/test-brunch")).andExpect(status().isForbidden());

      verify(brunchRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Should return 404 when brunch not found")
    void shouldReturn404WhenBrunchNotFound() throws Exception {
      setUpAdminAuthentication("non-existent");
      when(brunchRepository.existsById("non-existent")).thenReturn(false);

      mockMvc.perform(delete("/api/brunches/non-existent")).andExpect(status().isNotFound());

      verify(brunchRepository, never()).deleteById(anyString());
    }
  }

  @Nested
  @DisplayName("Create vote on brunch endpoint")
  class CreateVoteOnBrunchTests {

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVoteDTO)))
          .andExpect(status().isUnauthorized());

      verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    @DisplayName("Should return 404 when brunch not found")
    void shouldReturn404WhenBrunchNotFound() throws Exception {
      setUpVoterAuthentication("non-existent");
      when(brunchRepository.findById("non-existent")).thenReturn(Optional.empty());

      mockMvc
          .perform(
              post("/api/brunches/non-existent/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(sampleVoteDTO)))
          .andExpect(status().isNotFound());

      verify(voteRepository, never()).save(any(Vote.class));
    }
  }

  @Nested
  @DisplayName("Get results for brunch endpoint")
  class GetResultsForBrunchTests {

    @Test
    @DisplayName("Should return 403 when not authenticated")
    void shouldReturn403WhenNotAuthenticated() throws Exception {
      mockMvc.perform(get("/api/brunches/test-brunch/results")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when authenticated as voter (not admin)")
    void shouldReturn403WhenAuthenticatedAsVoter() throws Exception {
      setUpVoterAuthentication("test-brunch");

      mockMvc.perform(get("/api/brunches/test-brunch/results")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 when brunch not found")
    void shouldReturn404WhenBrunchNotFound() throws Exception {
      setUpAdminAuthentication("non-existent");
      when(brunchRepository.findById("non-existent")).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/brunches/non-existent/results")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Security tests")
  class SecurityTests {

    @Test
    @DisplayName("Should allow access to health check without authentication")
    void shouldAllowAccessToHealthCheckWithoutAuth() throws Exception {
      mockMvc.perform(get("/api/ok")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to protected endpoints in test configuration")
    void shouldAllowAccessToProtectedEndpoints() throws Exception {
      // Test security config allows all requests
      mockMvc.perform(get("/api/brunches")).andExpect(status().isOk());
    }
  }

  // Authentication helper methods
  private void setUpAdminAuthentication(String brunchId) {
    BrunchPasswordAuthenticationToken adminToken =
        BrunchPasswordAuthenticationToken.forAdmin(brunchId, "admin-password");
    adminToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(adminToken);
  }

  private void setUpVoterAuthentication(String brunchId) {
    BrunchPasswordAuthenticationToken voterToken =
        BrunchPasswordAuthenticationToken.forVoter(brunchId, "voter-password");
    voterToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(voterToken);
  }

  private void setupSampleData() {
    // Create sample BrunchCreateDTO
    sampleCreateDTO = new BrunchCreateDTO();
    sampleCreateDTO.setId("new-brunch");
    sampleCreateDTO.setTitle("New Test Brunch");
    sampleCreateDTO.setRequireEmail(true);
    sampleCreateDTO.setEmailRegexp(JsonNullable.of("/.*@test\\.com/"));
    sampleCreateDTO.setAdminPassword(JsonNullable.of("admin123"));
    sampleCreateDTO.setVotingPassword(JsonNullable.of("vote123"));

    BrunchQuestionDTO questionDTO = new BrunchQuestionDTO();
    questionDTO.setId(1);
    questionDTO.setTitle(JsonNullable.of("Sample Question"));
    questionDTO.setMin(1);
    questionDTO.setMax(5);
    questionDTO.setOptional(false);
    questionDTO.setOrder(JsonNullable.of(1));
    sampleCreateDTO.setQuestions(Set.of(questionDTO));

    // Create sample BrunchUpdateDTO
    sampleUpdateDTO = new BrunchUpdateDTO();
    sampleUpdateDTO.setTitle("Updated Test Brunch");
    sampleUpdateDTO.setRequireEmail(false);
    sampleUpdateDTO.setEmailRegexp(JsonNullable.of("/.*@updated\\.com/"));

    // Create sample BrunchVoteDTO
    sampleVoteDTO = new BrunchVoteDTO();
    sampleVoteDTO.setName("Test Voter");
    sampleVoteDTO.setEmail(JsonNullable.of("test@example.com"));

    BrunchAnswerDTO answerDTO = new BrunchAnswerDTO();
    answerDTO.setQuestionId(1);
    answerDTO.setValue(4);
    // Remove setConfidence as it doesn't exist in BrunchAnswerDTO
    sampleVoteDTO.setAnswers(Arrays.asList(answerDTO));

    // Create sample Brunch entity
    sampleBrunch =
        Brunch.builder()
            .id("test-brunch")
            .title("Test Brunch")
            .requireEmail(true)
            .emailRegexp(".*@test\\.com")
            .build();

    Question question = new Question();
    question.setId(1);
    question.setTitle("Sample Question");
    question.setMin(1);
    question.setMax(5);
    question.setBrunch(sampleBrunch);
    sampleBrunch.setQuestions(List.of(question));

    // Create sample Vote entity
    sampleVote = new Vote();
    sampleVote.setId(1);
    sampleVote.setName("Test Voter");
    sampleVote.setEmail("test@example.com");
    sampleVote.setBrunch(sampleBrunch);

    // Create sample BrunchInfoDTO
    sampleInfoDTO = new BrunchInfoDTO();
    sampleInfoDTO.setId("test-brunch");
    sampleInfoDTO.setTitle("Test Brunch");
    sampleInfoDTO.setRequireEmail(true);
    sampleInfoDTO.setEmailRegexp(JsonNullable.of("/.*@test\\.com/"));

    BrunchQuestionInfoDTO questionInfoDTO = new BrunchQuestionInfoDTO();
    questionInfoDTO.setId(1);
    questionInfoDTO.setTitle(JsonNullable.of("Sample Question"));
    questionInfoDTO.setMin(1);
    questionInfoDTO.setMax(5);
    questionInfoDTO.setOptional(false);
    questionInfoDTO.setOrder(1);
    sampleInfoDTO.setQuestions(Set.of(questionInfoDTO));
  }

  private BrunchInfoDTO createComplexBrunchInfoDTO() {
    BrunchInfoDTO complexDTO = new BrunchInfoDTO();
    complexDTO.setId("complex-brunch");
    complexDTO.setTitle("Complex Test Brunch");
    complexDTO.setRequireEmail(false);

    BrunchQuestionInfoDTO q1 = new BrunchQuestionInfoDTO();
    q1.setId(1);
    q1.setTitle(JsonNullable.of("First Question"));
    q1.setMin(0);
    q1.setMax(10);
    q1.setOptional(true);
    q1.setOrder(1);

    BrunchQuestionInfoDTO q2 = new BrunchQuestionInfoDTO();
    q2.setId(2);
    q2.setTitle(JsonNullable.of("Second Question"));
    q2.setMin(1);
    q2.setMax(5);
    q2.setOptional(false);
    q2.setOrder(2);

    complexDTO.setQuestions(Set.of(q1, q2));
    return complexDTO;
  }
}
