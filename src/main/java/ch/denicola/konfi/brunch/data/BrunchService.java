package ch.denicola.konfi.brunch.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;
import lombok.extern.java.Log;

@Service("brunchService")
@Log
public class BrunchService {

  @Autowired private ModelMapper modelMapper;

  public BrunchInfoDTO brunchToBrunchInfoDTO(Brunch brunch) {
    BrunchInfoDTO dto = new BrunchInfoDTO();

    dto.setId(brunch.getId());
    dto.setTitle(brunch.getTitle());
    dto.setRequireEmail(brunch.getRequireEmail());

    // Use JsonNullable wrappers
    dto.setEmailRegexp(JsonNullable.of(brunch.getEmailRegexp()));
    dto.setAdminPassword(JsonNullable.of(brunch.getAdminPassword()));
    dto.setVotingPassword(JsonNullable.of(brunch.getVotingPassword()));

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
        log.warning(e.getMessage());
        dto.setLink(JsonNullable.undefined());
      }
    } else {
      dto.setLink(JsonNullable.undefined());
    }

    dto.setOrder(question.getOrder());
    dto.setRecommended(question.getRecommended());

    return dto;
  }

  private URL uriToUrl(URI uri) {
    try {
      return uri != null ? uri.toURL() : null;
    } catch (MalformedURLException e) {
      log.warning(e.getMessage());
      return null;
    }
  }

  private Question mapToQuestionEntity(BrunchQuestionDTO dto, Brunch brunch) {
    Question question = new Question();

    question.setId(dto.getId()); // Optional; ignored by DB if null (auto-generated)

    question.setTitle(dto.getTitle().isPresent() ? dto.getTitle().get() : null);
    question.setLink(dto.getLink().isPresent() ? uriToUrl(dto.getLink().get()) : null);
    question.setMin(dto.getMin() != null ? dto.getMin() : 1);
    question.setMax(dto.getMax() != null ? dto.getMax() : 5);
    question.setOptional(Boolean.TRUE.equals(dto.getOptional()));

    question.setOrder(dto.getOrder().isPresent() ? dto.getOrder().get() : null);
    question.setRecommended(dto.getRecommended().isPresent() ? dto.getRecommended().get() : null);

    question.setBrunch(brunch); // Set owning side

    return question;
  }

  public Brunch brunchCreateDTOToBrunch(BrunchCreateDTO brunchCreate) {
    Brunch brunch = new Brunch();

    brunch.setId(brunchCreate.getId());
    brunch.setTitle(brunchCreate.getTitle());
    brunch.setRequireEmail(
        brunchCreate.getRequireEmail() != null ? brunchCreate.getRequireEmail() : false);
    brunch.setEmailRegexp(
        brunchCreate.getEmailRegexp().isPresent() ? brunchCreate.getEmailRegexp().get() : null);
    brunch.setAdminPassword(
        brunchCreate.getAdminPassword().isPresent() ? brunchCreate.getAdminPassword().get() : null);
    brunch.setVotingPassword(
        brunchCreate.getVotingPassword().isPresent()
            ? brunchCreate.getVotingPassword().get()
            : null);

    List<Question> questions =
        brunchCreate.getQuestions().stream()
            .map(q -> mapToQuestionEntity(q, brunch)) // pass the brunch as parent
            .sorted(
                Comparator.comparing(
                    Question::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());

    brunch.setQuestions(questions);

    return brunch;
  }
}
