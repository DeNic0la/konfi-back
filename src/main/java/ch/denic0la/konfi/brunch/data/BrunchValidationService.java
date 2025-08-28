package ch.denic0la.konfi.brunch.data;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.denic0la.openapi.konfi.brunch.model.BrunchAnswerDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchUpdateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;

@Service
public class BrunchValidationService {

  public void validateBrunchCreate(BrunchCreateDTO createDTO) {
    if (createDTO == null) {
      throw new IllegalArgumentException("BrunchCreateDTO cannot be null");
    }

    validateBrunchId(createDTO.getId());
    validateTitle(createDTO.getTitle());
    validateEmailRegexp(
        createDTO.getEmailRegexp() != null && createDTO.getEmailRegexp().isPresent()
            ? createDTO.getEmailRegexp().get()
            : null);

    if (createDTO.getQuestions() != null) {
      validateQuestions(new ArrayList<>(createDTO.getQuestions()));
    }

    validatePasswords(
        createDTO.getAdminPassword() != null ? createDTO.getAdminPassword().orElse(null) : null,
        createDTO.getVotingPassword() != null ? createDTO.getVotingPassword().orElse(null) : null);
  }

  public void validateBrunchUpdate(BrunchUpdateDTO updateDTO) {
    if (updateDTO == null) {
      throw new IllegalArgumentException("BrunchUpdateDTO cannot be null");
    }

    if (updateDTO.getTitle() != null) {
      validateTitle(updateDTO.getTitle());
    }

    if (updateDTO.getEmailRegexp() != null && updateDTO.getEmailRegexp().isPresent()) {
      validateEmailRegexp(updateDTO.getEmailRegexp().get());
    }

    if (updateDTO.getQuestions() != null) {
      validateQuestions(new ArrayList<>(updateDTO.getQuestions()));
    }

    validatePasswords(
        updateDTO.getAdminPassword() != null ? updateDTO.getAdminPassword().orElse(null) : null,
        updateDTO.getVotingPassword() != null ? updateDTO.getVotingPassword().orElse(null) : null);
  }

  public void validateVote(BrunchVoteDTO voteDTO, Brunch brunch) {
    if (voteDTO == null) {
      throw new IllegalArgumentException("BrunchVoteDTO cannot be null");
    }

    if (StringUtils.isBlank(voteDTO.getName())) {
      throw new IllegalArgumentException("Voter name cannot be empty");
    }

    // Validate email if required
    if (brunch.getRequireEmail() != null && brunch.getRequireEmail()) {
      String email =
          voteDTO.getEmail() != null && voteDTO.getEmail().isPresent()
              ? voteDTO.getEmail().get()
              : null;

      if (StringUtils.isBlank(email)) {
        throw new IllegalArgumentException("Email is required for this brunch");
      }

      validateEmail(email, brunch.getEmailRegexp());
    }

    // Validate answers
    if (voteDTO.getAnswers() != null) {
      validateAnswers(voteDTO.getAnswers(), brunch);
    }
  }

  private void validateBrunchId(String brunchId) {
    if (StringUtils.isBlank(brunchId)) {
      throw new IllegalArgumentException("Brunch ID cannot be empty");
    }

    if (brunchId.length() > 255) {
      throw new IllegalArgumentException("Brunch ID cannot exceed 255 characters");
    }

    // Allow alphanumeric, hyphens, underscores, and dots
    if (!brunchId.matches("^[a-zA-Z0-9._-]+$")) {
      throw new IllegalArgumentException(
          "Brunch ID contains invalid characters. Only letters, numbers, hyphens, underscores, and"
              + " dots are allowed");
    }
  }

  private void validateTitle(String title) {
    if (StringUtils.isBlank(title)) {
      throw new IllegalArgumentException("Title cannot be empty");
    }

    if (title.length() > 500) {
      throw new IllegalArgumentException("Title cannot exceed 500 characters");
    }
  }

  private void validateEmailRegexp(String emailRegexp) {
    if (StringUtils.isNotBlank(emailRegexp)) {
      try {
        Pattern.compile(emailRegexp);
      } catch (PatternSyntaxException e) {
        throw new IllegalArgumentException("Invalid email regular expression: " + e.getMessage());
      }
    }
  }

  private void validateQuestions(java.util.List<BrunchQuestionDTO> questions) {
    if (questions.isEmpty()) {
      throw new IllegalArgumentException("At least one question is required");
    }

    if (questions.size() > 50) {
      throw new IllegalArgumentException("Cannot exceed 50 questions per brunch");
    }

    for (BrunchQuestionDTO question : questions) {
      validateQuestion(question);
    }
  }

  private void validateQuestion(BrunchQuestionDTO question) {
    if (question == null) {
      throw new IllegalArgumentException("Question cannot be null");
    }

    // Validate title if present
    if (question.getTitle() != null && question.getTitle().isPresent()) {
      String title = question.getTitle().get();
      if (StringUtils.isNotBlank(title) && title.length() > 500) {
        throw new IllegalArgumentException("Question title cannot exceed 500 characters");
      }
    }

    // Validate min/max range
    Integer min = question.getMin() != null ? question.getMin() : 1;
    Integer max = question.getMax() != null ? question.getMax() : 5;

    if (min < 1 || min > 10) {
      throw new IllegalArgumentException("Question minimum value must be between 1 and 10");
    }

    if (max < 1 || max > 10) {
      throw new IllegalArgumentException("Question maximum value must be between 1 and 10");
    }

    if (min > max) {
      throw new IllegalArgumentException(
          "Question minimum value cannot be greater than maximum value");
    }

    // Validate order if present
    if (question.getOrder() != null && question.getOrder().isPresent()) {
      Integer order = question.getOrder().get();
      if (order < 0) {
        throw new IllegalArgumentException("Question order cannot be negative");
      }
    }
  }

  private void validatePasswords(String adminPassword, String votingPassword) {
    // Both passwords being null/empty is allowed (passwordless brunch)
    // But if provided, they should have minimum requirements

    if (StringUtils.isNotBlank(adminPassword)) {
      validatePassword(adminPassword, "Admin password");
    }

    if (StringUtils.isNotBlank(votingPassword)) {
      validatePassword(votingPassword, "Voting password");
    }
  }

  private void validatePassword(String password, String passwordType) {
    if (password.length() < 4) {
      throw new IllegalArgumentException(passwordType + " must be at least 4 characters long");
    }

    if (password.length() > 100) {
      throw new IllegalArgumentException(passwordType + " cannot exceed 100 characters");
    }
  }

  private void validateEmail(String email, String emailRegexp) {
    // Basic email validation
    if (!email.contains("@")) {
      throw new IllegalArgumentException("Invalid email format");
    }

    // Custom regexp validation if provided
    if (StringUtils.isNotBlank(emailRegexp)) {
      try {
        Pattern pattern = Pattern.compile(emailRegexp);
        if (!pattern.matcher(email).matches()) {
          throw new IllegalArgumentException("Email does not match required format");
        }
      } catch (PatternSyntaxException e) {
        // Log warning but don't fail validation if regexp is invalid
        // (this should have been caught during brunch creation)
      }
    }
  }

  private void validateAnswers(java.util.List<BrunchAnswerDTO> answers, Brunch brunch) {
    // Validate each answer
    for (BrunchAnswerDTO answer : answers) {
      validateAnswer(answer, brunch);
    }

    // Check for mandatory questions
    java.util.Set<Integer> answeredQuestionIds = new java.util.HashSet<>();
    for (BrunchAnswerDTO answer : answers) {
      answeredQuestionIds.add(answer.getQuestionId());
    }

    for (Question question : brunch.getQuestions()) {
      if (!Boolean.TRUE.equals(question.getOptional())
          && !answeredQuestionIds.contains(question.getId())) {
        throw new IllegalArgumentException(
            "Answer required for question: "
                + (question.getTitle() != null ? question.getTitle() : "ID " + question.getId()));
      }
    }
  }

  private void validateAnswer(BrunchAnswerDTO answer, Brunch brunch) {
    if (answer == null) {
      throw new IllegalArgumentException("Answer cannot be null");
    }

    if (answer.getQuestionId() == null) {
      throw new IllegalArgumentException("Answer must specify a question ID");
    }

    if (answer.getValue() == null) {
      throw new IllegalArgumentException("Answer value cannot be null");
    }

    // Find the corresponding question to validate the range
    Question question =
        brunch.getQuestions().stream()
            .filter(q -> q.getId().equals(answer.getQuestionId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Question not found: " + answer.getQuestionId()));

    Integer min = question.getMin() != null ? question.getMin() : 1;
    Integer max = question.getMax() != null ? question.getMax() : 5;

    if (answer.getValue() < min || answer.getValue() > max) {
      throw new IllegalArgumentException(
          "Answer value "
              + answer.getValue()
              + " is out of range ["
              + min
              + "-"
              + max
              + "] for question "
              + answer.getQuestionId());
    }
  }
}
