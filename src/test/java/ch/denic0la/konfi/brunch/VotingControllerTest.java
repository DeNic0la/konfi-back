package ch.denic0la.konfi.brunch;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.denic0la.konfi.brunch.data.Brunch;
import ch.denic0la.konfi.brunch.data.BrunchManagementService;
import ch.denic0la.konfi.brunch.data.BrunchMappingService;
import ch.denic0la.konfi.brunch.data.BrunchRepository;
import ch.denic0la.konfi.brunch.data.BrunchService;
import ch.denic0la.konfi.brunch.data.BrunchValidationService;
import ch.denic0la.konfi.brunch.data.Vote;
import ch.denic0la.konfi.brunch.data.VoteRepository;
import ch.denic0la.konfi.brunch.data.VotingService;
import ch.denic0la.konfi.brunch.security.BrunchPasswordAuthenticationToken;
import ch.denic0la.konfi.config.TestSecurityConfig;
import ch.denic0la.konfi.testutils.TestDataFactory;
import ch.denic0la.openapi.konfi.brunch.model.BrunchAnswerDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VotingController.class)
@Import(TestSecurityConfig.class)
class VotingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private BrunchService brunchService;
  @MockBean private BrunchRepository brunchRepository;
  @MockBean private VoteRepository voteRepository;
  @MockBean private BrunchManagementService brunchManagementService;
  @MockBean private BrunchMappingService brunchMappingService;
  @MockBean private BrunchValidationService brunchValidationService;
  @MockBean private VotingService votingService;

  private Brunch testBrunch;
  private BrunchVoteDTO validVoteDTO;

  @BeforeEach
  void setUp() {
    testBrunch = TestDataFactory.createTestBrunch();
    validVoteDTO = createValidVoteDTO();
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("Vote Submission Tests")
  class VoteSubmissionTests {

    @Test
    @DisplayName("Should successfully submit vote with voter authentication")
    @WithMockUser
    void shouldSuccessfullySubmitVoteWithVoterAuth() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));
      Vote mockVote = TestDataFactory.createTestVote(testBrunch);
      when(brunchService.brunchVoteDTOToVote(validVoteDTO, testBrunch)).thenReturn(mockVote);
      when(voteRepository.save(mockVote)).thenReturn(mockVote);

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isOk());

      verify(voteRepository).save(mockVote);
    }

    @Test
    @DisplayName("Should successfully submit vote with admin authentication")
    @WithMockUser
    void shouldSuccessfullySubmitVoteWithAdminAuth() throws Exception {
      // Given
      setupAdminAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));
      Vote mockVote = TestDataFactory.createTestVote(testBrunch);
      when(brunchService.brunchVoteDTOToVote(validVoteDTO, testBrunch)).thenReturn(mockVote);
      when(voteRepository.save(mockVote)).thenReturn(mockVote);

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isOk());

      verify(voteRepository).save(mockVote);
    }

    @Test
    @DisplayName("Should reject vote submission without authentication")
    void shouldRejectVoteWithoutAuth() throws Exception {
      // Given - no authentication set up

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isUnauthorized());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vote for non-existent brunch")
    @WithMockUser
    void shouldRejectVoteForNonExistentBrunch() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("non-existent")).thenReturn(Optional.empty());

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/non-existent/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isNotFound());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vote with empty name")
    @WithMockUser
    void shouldRejectVoteWithEmptyName() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      BrunchVoteDTO invalidVote = createValidVoteDTO();
      invalidVote.setName("");

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidVote)))
          .andExpect(status().isBadRequest());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vote with null name")
    @WithMockUser
    void shouldRejectVoteWithNullName() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      BrunchVoteDTO invalidVote = createValidVoteDTO();
      invalidVote.setName(null);

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidVote)))
          .andExpect(status().isBadRequest());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vote when email required but not provided")
    @WithMockUser
    void shouldRejectVoteWhenEmailRequiredButNotProvided() throws Exception {
      // Given
      setupVoterAuthentication();
      testBrunch.setRequireEmail(true);
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      BrunchVoteDTO voteWithoutEmail = createValidVoteDTO();
      voteWithoutEmail.setEmail(JsonNullable.undefined());

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(voteWithoutEmail)))
          .andExpect(status().isBadRequest());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vote when email format doesn't match regex")
    @WithMockUser
    void shouldRejectVoteWhenEmailFormatDoesntMatchRegex() throws Exception {
      // Given
      setupVoterAuthentication();
      testBrunch.setRequireEmail(true);
      testBrunch.setEmailRegexp(".*@company\\.com$");
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      BrunchVoteDTO invalidEmailVote = createValidVoteDTO();
      invalidEmailVote.setEmail(JsonNullable.of("user@other.com"));

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidEmailVote)))
          .andExpect(status().isBadRequest());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept vote when email format matches regex")
    @WithMockUser
    void shouldAcceptVoteWhenEmailFormatMatchesRegex() throws Exception {
      // Given
      setupVoterAuthentication();
      testBrunch.setRequireEmail(true);
      testBrunch.setEmailRegexp(".*@company\\.com$");
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      BrunchVoteDTO validEmailVote = createValidVoteDTO();
      validEmailVote.setEmail(JsonNullable.of("user@company.com"));

      Vote mockVote = TestDataFactory.createTestVote(testBrunch);
      when(brunchService.brunchVoteDTOToVote(validEmailVote, testBrunch)).thenReturn(mockVote);
      when(voteRepository.save(mockVote)).thenReturn(mockVote);

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validEmailVote)))
          .andExpect(status().isOk());

      verify(voteRepository).save(mockVote);
    }

    @Test
    @DisplayName("Should handle service layer validation errors")
    @WithMockUser
    void shouldHandleServiceLayerValidationErrors() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));
      when(brunchService.brunchVoteDTOToVote(validVoteDTO, testBrunch))
          .thenThrow(new IllegalArgumentException("Invalid answer value"));

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isBadRequest());

      verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle unexpected errors gracefully")
    @WithMockUser
    void shouldHandleUnexpectedErrors() throws Exception {
      // Given
      setupVoterAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));
      Vote mockVote = TestDataFactory.createTestVote(testBrunch);
      when(brunchService.brunchVoteDTOToVote(validVoteDTO, testBrunch)).thenReturn(mockVote);
      when(voteRepository.save(mockVote)).thenThrow(new RuntimeException("Database error"));

      // When & Then
      mockMvc
          .perform(
              post("/api/brunches/test-brunch/vote")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validVoteDTO)))
          .andExpect(status().isInternalServerError());
    }
  }

  @Nested
  @DisplayName("Results Retrieval Tests")
  class ResultsRetrievalTests {

    @Test
    @DisplayName("Should successfully retrieve results with admin authentication")
    @WithMockUser
    void shouldRetrieveResultsWithAdminAuth() throws Exception {
      // Given
      setupAdminAuthentication();
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      List<BrunchVoteDTO> expectedResults = List.of(validVoteDTO);
      List<Vote> mockVotes = List.of(TestDataFactory.createTestVote(testBrunch));
      testBrunch.setVotes(mockVotes);

      when(brunchService.voteToBrunchVoteDTO(any(Vote.class))).thenReturn(validVoteDTO);

      // When & Then
      mockMvc
          .perform(get("/api/brunches/test-brunch/results"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].name").value("Test Voter"));
    }

    @Test
    @DisplayName("Should reject results retrieval with voter authentication")
    @WithMockUser
    void shouldRejectResultsWithVoterAuth() throws Exception {
      // Given
      setupVoterAuthentication();

      // When & Then
      mockMvc.perform(get("/api/brunches/test-brunch/results")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject results retrieval without authentication")
    void shouldRejectResultsWithoutAuth() throws Exception {
      // Given - no authentication set up

      // When & Then
      mockMvc.perform(get("/api/brunches/test-brunch/results")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject results retrieval for non-existent brunch")
    @WithMockUser
    void shouldRejectResultsForNonExistentBrunch() throws Exception {
      // Given
      setupAdminAuthentication();
      when(brunchRepository.findById("non-existent")).thenReturn(Optional.empty());

      // When & Then
      mockMvc.perform(get("/api/brunches/non-existent/results")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty results for brunch with no votes")
    @WithMockUser
    void shouldReturnEmptyResultsForBrunchWithNoVotes() throws Exception {
      // Given
      setupAdminAuthentication();
      testBrunch.setVotes(List.of());
      when(brunchRepository.findById("test-brunch")).thenReturn(Optional.of(testBrunch));

      // When & Then
      mockMvc
          .perform(get("/api/brunches/test-brunch/results"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  private void setupVoterAuthentication() {
    BrunchPasswordAuthenticationToken voterToken =
        BrunchPasswordAuthenticationToken.forVoter("test-brunch", "voter-pass");
    voterToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(voterToken);
  }

  private void setupAdminAuthentication() {
    BrunchPasswordAuthenticationToken adminToken =
        BrunchPasswordAuthenticationToken.forAdmin("test-brunch", "admin-pass");
    adminToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(adminToken);
  }

  private BrunchVoteDTO createValidVoteDTO() {
    BrunchVoteDTO voteDTO = new BrunchVoteDTO();
    voteDTO.setName("Test Voter");
    voteDTO.setEmail(JsonNullable.of("test@example.com"));

    BrunchAnswerDTO answer = new BrunchAnswerDTO();
    answer.setQuestionId(1);
    answer.setValue(3);
    voteDTO.setAnswers(List.of(answer));

    return voteDTO;
  }
}
