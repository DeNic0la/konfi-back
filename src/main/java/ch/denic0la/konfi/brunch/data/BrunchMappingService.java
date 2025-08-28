package ch.denic0la.konfi.brunch.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.denic0la.konfi.KonfiApplication;
import ch.denic0la.openapi.konfi.brunch.model.BrunchAnswerDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchUpdateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;
import lombok.extern.java.Log;

@Service
@Log
public class BrunchMappingService {

  @Autowired private ModelMapper modelMapper;
  @Autowired private QuestionRepository questionRepository;
  private final PasswordEncoder passwordEncoder = KonfiApplication.getPasswordEncoder();

  public BrunchInfoDTO brunchToBrunchInfoDTO(Brunch brunch) {
    BrunchInfoDTO dto = new BrunchInfoDTO();

    dto.setId(brunch.getId());
    dto.setTitle(brunch.getTitle());
    dto.setRequireEmail(brunch.getRequireEmail());

    // Use JsonNullable wrappers
    dto.setEmailRegexp(JsonNullable.of(brunch.getEmailRegexp()));

    // Convert List<Question> to Set<BrunchQuestionInfoDTO>
    Set<BrunchQuestionInfoDTO> questionDTOs =
        brunch.getQuestions().stream()
            .map(this::questionToBrunchQuestionInfoDTO)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    dto.setQuestions(questionDTOs);

    return dto;
  }

  public BrunchQuestionInfoDTO questionToBrunchQuestionInfoDTO(Question question) {
    BrunchQuestionInfoDTO dto = new BrunchQuestionInfoDTO();

    dto.setId(question.getId());
    dto.setMin(question.getMin() != null ? question.getMin() : 1);
    dto.setMax(question.getMax() != null ? question.getMax() : 5);
    dto.setTitle(JsonNullable.of(question.getTitle()));
    dto.setOptional(Boolean.TRUE.equals(question.getOptional()));

    if (question.getLink() != null) {
      try {
        dto.setLink(JsonNullable.of(question.getLink().toURI()));
      } catch (URISyntaxException e) {
        log.warning(
            "Failed to convert URL to URI for question "
                + question.getId()
                + ": "
                + question.getLink()
                + ". Error: "
                + e.getMessage());
        dto.setLink(JsonNullable.undefined());
      }
    } else {
      dto.setLink(JsonNullable.undefined());
    }

    dto.setOrder(question.getOrder());
    dto.setRecommended(question.getRecommended());

    return dto;
  }

  public Brunch brunchCreateDTOToBrunch(BrunchCreateDTO brunchCreate) {
    Brunch brunch = new Brunch();

    brunch.setId(brunchCreate.getId());
    brunch.setTitle(brunchCreate.getTitle());
    brunch.setRequireEmail(
        brunchCreate.getRequireEmail() != null ? brunchCreate.getRequireEmail() : false);
    brunch.setEmailRegexp(
        brunchCreate.getEmailRegexp() != null && brunchCreate.getEmailRegexp().isPresent()
            ? brunchCreate.getEmailRegexp().get()
            : null);

    // Create authorization with properly hashed passwords
    var builtAuth = createBrunchAuthorization(brunchCreate, brunch);
    brunch.setBrunchAuthorization(builtAuth);

    List<Question> questions =
        brunchCreate.getQuestions().stream()
            .map(q -> mapToQuestionEntity(q, brunch))
            .sorted(
                Comparator.comparing(
                    Question::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

    brunch.setQuestions(questions);

    return brunch;
  }

  public Brunch updateBrunch(Brunch existingBrunch, BrunchUpdateDTO updateDTO) {
    // Update basic fields if present
    if (updateDTO.getTitle() != null) {
      existingBrunch.setTitle(updateDTO.getTitle());
    }
    if (updateDTO.getRequireEmail() != null) {
      existingBrunch.setRequireEmail(updateDTO.getRequireEmail());
    }
    if (updateDTO.getEmailRegexp() != null && updateDTO.getEmailRegexp().isPresent()) {
      existingBrunch.setEmailRegexp(updateDTO.getEmailRegexp().get());
    } else if (updateDTO.getEmailRegexp() != null) {
      existingBrunch.setEmailRegexp(null);
    }

    // Update questions if present
    if (updateDTO.getQuestions() != null) {
      updateQuestions(existingBrunch, new ArrayList<>(updateDTO.getQuestions()));
    }

    // Update passwords if present with proper hashing
    updatePasswords(existingBrunch, updateDTO);

    return existingBrunch;
  }

  public Vote brunchVoteDTOToVote(BrunchVoteDTO voteDTO, Brunch brunch) {
    Vote vote = new Vote();
    vote.setName(voteDTO.getName());
    vote.setBrunch(brunch);

    if (voteDTO.getEmail() != null && voteDTO.getEmail().isPresent()) {
      vote.setEmail(voteDTO.getEmail().get());
    }

    List<VoteAnswer> voteAnswers = new ArrayList<>();
    if (voteDTO.getAnswers() != null) {
      for (BrunchAnswerDTO answerDTO : voteDTO.getAnswers()) {
        VoteAnswer voteAnswer = createVoteAnswer(answerDTO, vote);
        voteAnswers.add(voteAnswer);
      }
    }
    vote.setVoteAnswers(voteAnswers);

    return vote;
  }

  public BrunchVoteDTO voteToBrunchVoteDTO(Vote vote) {
    BrunchVoteDTO dto = new BrunchVoteDTO();
    dto.setName(vote.getName());
    dto.setEmail(JsonNullable.of(vote.getEmail()));

    List<BrunchAnswerDTO> answerDTOs =
        vote.getVoteAnswers().stream()
            .map(this::voteAnswerToBrunchAnswerDTO)
            .collect(Collectors.toList());
    dto.setAnswers(answerDTOs);

    return dto;
  }

  private BrunchAuthorization createBrunchAuthorization(
      BrunchCreateDTO brunchCreate, Brunch brunch) {
    return BrunchAuthorization.builder()
        .brunch(brunch)
        .brunchId(brunchCreate.getId())
        .adminPasswordHash(
            hashPasswordIfPresent(
                brunchCreate.getAdminPassword() != null
                    ? brunchCreate.getAdminPassword().orElse(null)
                    : null))
        .votingPasswordHash(
            hashPasswordIfPresent(
                brunchCreate.getVotingPassword() != null
                    ? brunchCreate.getVotingPassword().orElse(null)
                    : null))
        .build();
  }

  private void updatePasswords(Brunch existingBrunch, BrunchUpdateDTO updateDTO) {
    if (existingBrunch.getBrunchAuthorization() != null) {
      BrunchAuthorization auth = existingBrunch.getBrunchAuthorization();

      if (updateDTO.getAdminPassword() != null && updateDTO.getAdminPassword().isPresent()) {
        String newPassword = updateDTO.getAdminPassword().get();
        auth.setAdminPasswordHash(hashPasswordIfPresent(newPassword));
      }

      if (updateDTO.getVotingPassword() != null && updateDTO.getVotingPassword().isPresent()) {
        String newPassword = updateDTO.getVotingPassword().get();
        auth.setVotingPasswordHash(hashPasswordIfPresent(newPassword));
      }
    }
  }

  private String hashPasswordIfPresent(String password) {
    if (password != null && !password.isEmpty()) {
      return passwordEncoder.encode(password);
    }
    return null;
  }

  private void updateQuestions(Brunch brunch, List<BrunchQuestionDTO> questionDTOs) {
    // Clear existing questions
    brunch.getQuestions().clear();

    // Add new questions
    List<Question> newQuestions =
        questionDTOs.stream()
            .map(q -> mapToQuestionEntity(q, brunch))
            .sorted(
                Comparator.comparing(
                    Question::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

    brunch.setQuestions(newQuestions);
  }

  private Question mapToQuestionEntity(BrunchQuestionDTO dto, Brunch brunch) {
    Question question = new Question();

    // ID is auto-generated by JPA, don't set it manually

    question.setTitle(
        dto.getTitle() != null && dto.getTitle().isPresent() ? dto.getTitle().get() : null);
    question.setLink(
        dto.getLink() != null && dto.getLink().isPresent() ? uriToUrl(dto.getLink().get()) : null);
    question.setMin(dto.getMin() != null ? dto.getMin() : 1);
    question.setMax(dto.getMax() != null ? dto.getMax() : 5);
    question.setOptional(Boolean.TRUE.equals(dto.getOptional()));

    question.setOrder(
        dto.getOrder() != null && dto.getOrder().isPresent() ? dto.getOrder().get() : null);
    question.setRecommended(
        dto.getRecommended() != null && dto.getRecommended().isPresent()
            ? dto.getRecommended().get()
            : null);

    question.setBrunch(brunch); // Set owning side

    return question;
  }

  private VoteAnswer createVoteAnswer(BrunchAnswerDTO answerDTO, Vote vote) {
    VoteAnswer voteAnswer = new VoteAnswer();
    voteAnswer.setVote(vote);
    voteAnswer.setKonfidenceValue(answerDTO.getValue());

    // Find the question by ID
    Question question =
        questionRepository
            .findById(answerDTO.getQuestionId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Question not found: " + answerDTO.getQuestionId()));
    voteAnswer.setAnswerTo(question);

    return voteAnswer;
  }

  private BrunchAnswerDTO voteAnswerToBrunchAnswerDTO(VoteAnswer voteAnswer) {
    BrunchAnswerDTO dto = new BrunchAnswerDTO();
    dto.setQuestionId(voteAnswer.getAnswerTo().getId());
    dto.setValue(voteAnswer.getKonfidenceValue());
    return dto;
  }

  private URL uriToUrl(URI uri) {
    if (uri == null) {
      return null;
    }
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      log.warning("Failed to convert URI to URL: " + uri + ". Error: " + e.getMessage());
      return null;
    } catch (IllegalArgumentException e) {
      log.warning("Invalid URI format: " + uri + ". Error: " + e.getMessage());
      return null;
    }
  }
}
