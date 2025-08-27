package ch.denic0la.konfi.testutils;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openapitools.jackson.nullable.JsonNullable;

import ch.denic0la.konfi.brunch.data.Brunch;
import ch.denic0la.konfi.brunch.data.BrunchAuthorization;
import ch.denic0la.konfi.brunch.data.Question;
import ch.denic0la.konfi.brunch.data.Vote;
import ch.denic0la.konfi.brunch.data.VoteAnswer;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;

public class TestDataFactory {

  // JsonNullable utility methods to reduce boilerplate
  private static <T> JsonNullable<T> nullable(T value) {
    return value != null ? JsonNullable.of(value) : JsonNullable.<T>undefined();
  }

  private static <T> JsonNullable<T> undefinedNullable() {
    return JsonNullable.<T>undefined();
  }

  private static <T> JsonNullable<T> ofNullable(T value) {
    return JsonNullable.of(value);
  }

  // RelationshipHelper for managing bidirectional JPA relationships
  public static class RelationshipHelper {
    public static Brunch brunchWithQuestions(Question... questions) {
      Brunch brunch = Brunch.builder().build();
      for (Question question : questions) {
        question.setBrunch(brunch);
        brunch.getQuestions().add(question);
      }
      return brunch;
    }

    public static Vote voteWithAnswers(VoteAnswer... answers) {
      Vote vote = Vote.builder().build();
      for (VoteAnswer answer : answers) {
        answer.setVote(vote);
        vote.getVoteAnswers().add(answer);
      }
      return vote;
    }

    public static Brunch brunchWithVotes(Vote... votes) {
      Brunch brunch = Brunch.builder().build();
      for (Vote vote : votes) {
        vote.setBrunch(brunch);
        brunch.getVotes().add(vote);
      }
      return brunch;
    }
  }

  // Brunch factory methods using Lombok-generated builder
  public static Brunch.BrunchBuilder brunchBuilder() {
    return Brunch.builder().id("test-brunch").title("Test Brunch").requireEmail(false);
  }

  public static Brunch brunch() {
    return brunchBuilder().build();
  }

  public static Brunch brunch(String id, String title) {
    return brunchBuilder().id(id).title(title).build();
  }

  public static Brunch brunchWithAuth(
      String id, String title, String adminPassword, String votingPassword) {
    Brunch brunch = brunch(id, title);
    BrunchAuthorization auth =
        BrunchAuthorization.builder()
            .brunch_id(id)
            .adminPasswordHash(adminPassword)
            .votingPasswordHash(votingPassword)
            .brunch(brunch)
            .build();
    brunch.setBrunchAuthorization(auth);
    return brunch;
  }

  // Question factory methods using Lombok-generated builder
  public static Question.QuestionBuilder questionBuilder() {
    return Question.builder().title("Test Question").min(1).max(5).optional(false).order(1);
  }

  public static Question question() {
    return questionBuilder().build();
  }

  public static Question question(String title) {
    return questionBuilder().title(title).build();
  }

  public static Question question(String title, Integer order) {
    return questionBuilder().title(title).order(order).build();
  }

  public static Question questionWithLink(String title, String linkUrl) {
    try {
      return questionBuilder().title(title).link(URI.create(linkUrl).toURL()).build();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL: " + linkUrl, e);
    }
  }

  // Optimized DTO builders with JsonNullable utilities
  public static class BrunchCreateDTOBuilder {
    private String id = "test-brunch-dto";
    private String title = "Test Brunch DTO";
    private Boolean requireEmail = false;
    private JsonNullable<String> emailRegexp = undefinedNullable();
    private JsonNullable<String> adminPassword = ofNullable("admin");
    private JsonNullable<String> votingPassword = ofNullable("vote");
    private Set<BrunchQuestionDTO> questions = new LinkedHashSet<>();

    public BrunchCreateDTOBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public BrunchCreateDTOBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    public BrunchCreateDTOBuilder withEmailRequirement(boolean requireEmail, String emailRegexp) {
      this.requireEmail = requireEmail;
      this.emailRegexp = nullable(emailRegexp);
      return this;
    }

    public BrunchCreateDTOBuilder withPasswords(String adminPassword, String votingPassword) {
      this.adminPassword = nullable(adminPassword);
      this.votingPassword = nullable(votingPassword);
      return this;
    }

    public BrunchCreateDTOBuilder withQuestions(Set<BrunchQuestionDTO> questions) {
      this.questions = questions;
      return this;
    }

    public BrunchCreateDTO build() {
      BrunchCreateDTO dto = new BrunchCreateDTO();
      dto.setId(id);
      dto.setTitle(title);
      dto.setRequireEmail(requireEmail);
      dto.setEmailRegexp(emailRegexp);
      dto.setAdminPassword(adminPassword);
      dto.setVotingPassword(votingPassword);
      dto.setQuestions(questions);
      return dto;
    }
  }

  public static class BrunchQuestionDTOBuilder {
    private Integer id = 1;
    private JsonNullable<String> title = ofNullable("Test Question DTO");
    private Integer min = 1;
    private Integer max = 5;
    private Boolean optional = false;
    private JsonNullable<Integer> order = ofNullable(1);
    private JsonNullable<Integer> recommended = undefinedNullable();
    private JsonNullable<URI> link = undefinedNullable();

    public BrunchQuestionDTOBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public BrunchQuestionDTOBuilder withTitle(String title) {
      this.title = nullable(title);
      return this;
    }

    public BrunchQuestionDTOBuilder withRange(Integer min, Integer max) {
      this.min = min;
      this.max = max;
      return this;
    }

    public BrunchQuestionDTOBuilder asOptional(boolean optional) {
      this.optional = optional;
      return this;
    }

    public BrunchQuestionDTOBuilder withOrder(Integer order) {
      this.order = nullable(order);
      return this;
    }

    public BrunchQuestionDTOBuilder withRecommended(Integer recommended) {
      this.recommended = nullable(recommended);
      return this;
    }

    public BrunchQuestionDTOBuilder withLink(String linkUrl) {
      this.link = nullable(URI.create(linkUrl));
      return this;
    }

    public BrunchQuestionDTO build() {
      BrunchQuestionDTO dto = new BrunchQuestionDTO();
      dto.setId(id);
      dto.setTitle(title);
      dto.setMin(min);
      dto.setMax(max);
      dto.setOptional(optional);
      dto.setOrder(order);
      dto.setRecommended(recommended);
      dto.setLink(link);
      return dto;
    }
  }

  // Vote factory methods using Lombok-generated builder
  public static Vote.VoteBuilder voteBuilder() {
    return Vote.builder().name("Test Voter");
  }

  public static Vote vote() {
    return voteBuilder().build();
  }

  public static Vote vote(String name) {
    return voteBuilder().name(name).build();
  }

  public static Vote vote(String name, String email) {
    return voteBuilder().name(name).email(email).build();
  }

  // VoteAnswer factory methods using Lombok-generated builder
  public static VoteAnswer.VoteAnswerBuilder voteAnswerBuilder() {
    return VoteAnswer.builder().konfidenceValue(5);
  }

  public static VoteAnswer voteAnswer() {
    return voteAnswerBuilder().build();
  }

  public static VoteAnswer voteAnswer(Integer konfidenceValue) {
    return voteAnswerBuilder().konfidenceValue(konfidenceValue).build();
  }

  public static VoteAnswer voteAnswer(Question question, Integer konfidenceValue) {
    return voteAnswerBuilder().answerTo(question).konfidenceValue(konfidenceValue).build();
  }

  // Static factory methods for DTO builders
  public static BrunchCreateDTOBuilder brunchCreateDTO() {
    return new BrunchCreateDTOBuilder();
  }

  public static BrunchQuestionDTOBuilder brunchQuestionDTO() {
    return new BrunchQuestionDTOBuilder();
  }

  // Common test data presets
  public static Brunch createSimpleBrunch(String id, String title) {
    return brunch(id, title);
  }

  public static BrunchCreateDTO createSimpleBrunchCreateDTO(String id, String title) {
    return brunchCreateDTO().withId(id).withTitle(title).build();
  }

  public static Question createSimpleQuestion(String title, int order) {
    return question(title, order);
  }

  public static BrunchQuestionDTO createSimpleBrunchQuestionDTO(String title, int order) {
    return brunchQuestionDTO().withTitle(title).withOrder(order).build();
  }

  // Test scenario records for common use cases
  public record TestScenario(String name, Brunch brunch, List<Vote> votes, boolean isComplete) {
    public static TestScenario simple() {
      Question q1 = question("What time works best?", 1);
      Question q2 = question("Food preferences?", 2);
      Brunch brunch = RelationshipHelper.brunchWithQuestions(q1, q2);

      return new TestScenario("Simple Brunch", brunch, List.of(vote("Alice"), vote("Bob")), false);
    }

    public static TestScenario withVoting() {
      Question q1 = question("What time works best?", 1);
      Brunch brunch = RelationshipHelper.brunchWithQuestions(q1);

      VoteAnswer answer1 = voteAnswer(q1, 4);
      VoteAnswer answer2 = voteAnswer(q1, 3);

      Vote vote1 = RelationshipHelper.voteWithAnswers(answer1);
      Vote vote2 = RelationshipHelper.voteWithAnswers(answer2);

      return new TestScenario(
          "Brunch with Votes",
          RelationshipHelper.brunchWithVotes(vote1, vote2),
          List.of(vote1, vote2),
          true);
    }

    public static TestScenario withAuthentication() {
      Question q1 = question("When should we meet?", 1);
      Brunch brunch = brunchWithAuth("secure-brunch", "Team Lunch", "admin123", "vote456");
      brunch.getQuestions().add(q1);
      q1.setBrunch(brunch);

      return new TestScenario("Authenticated Brunch", brunch, List.of(), false);
    }

    public static TestScenario emailRequired() {
      Question q1 = question("Dietary restrictions?", 1);
      Brunch brunch =
          brunchBuilder()
              .id("email-brunch")
              .title("Company Event")
              .requireEmail(true)
              .emailRegexp(".*@company\\.com")
              .build();

      brunch.getQuestions().add(q1);
      q1.setBrunch(brunch);

      return new TestScenario(
          "Email Required Brunch", brunch, List.of(vote("John Doe", "john@company.com")), false);
    }
  }
}
