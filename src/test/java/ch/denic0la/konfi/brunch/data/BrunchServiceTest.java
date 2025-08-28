package ch.denic0la.konfi.brunch.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BrunchServiceTest {

  @Autowired private BrunchService brunchService;

  @BeforeEach
  void setUp() {
    // No manual setup needed - Spring Boot will wire dependencies
  }

  @Nested
  @DisplayName("Brunch to BrunchInfoDTO conversion")
  class BrunchToBrunchInfoDTOTests {

    @Test
    @DisplayName("Should convert basic brunch entity to DTO")
    void shouldConvertBasicBrunchToDTO() {
      Brunch brunch =
          Brunch.builder()
              .id("test-brunch-id")
              .title("Test Brunch")
              .requireEmail(true)
              .emailRegexp(".*@example\\.com")
              .questions(List.of())
              .build();

      BrunchInfoDTO result = brunchService.brunchToBrunchInfoDTO(brunch);

      assertThat(result.getId()).isEqualTo("test-brunch-id");
      assertThat(result.getTitle()).isEqualTo("Test Brunch");
      assertThat(result.getRequireEmail()).isTrue();
      assertThat(result.getEmailRegexp().isPresent()).isTrue();
      assertThat(result.getEmailRegexp().get()).isEqualTo(".*@example\\.com");
      assertThat(result.getQuestions()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null email regexp")
    void shouldHandleNullEmailRegexp() {
      Brunch brunch =
          Brunch.builder()
              .id("test-brunch")
              .title("Test")
              .requireEmail(false)
              .emailRegexp(null)
              .questions(List.of())
              .build();

      BrunchInfoDTO result = brunchService.brunchToBrunchInfoDTO(brunch);

      assertThat(result.getEmailRegexp().isPresent()).isTrue();
      assertThat(result.getEmailRegexp().get()).isNull();
    }

    @Test
    @DisplayName("Should convert brunch with questions")
    void shouldConvertBrunchWithQuestions() throws MalformedURLException {
      Question question1 = new Question();
      question1.setId(1);
      question1.setTitle("Question 1");
      question1.setMin(1);
      question1.setMax(5);
      question1.setOptional(false);
      question1.setOrder(1);
      question1.setLink(URI.create("https://example.com").toURL());

      Question question2 = new Question();
      question2.setId(2);
      question2.setTitle("Question 2");
      question2.setMin(0);
      question2.setMax(10);
      question2.setOptional(true);
      question2.setOrder(2);

      Brunch brunch =
          Brunch.builder()
              .id("brunch-with-questions")
              .title("Brunch with Questions")
              .requireEmail(false)
              .questions(List.of(question1, question2))
              .build();

      BrunchInfoDTO result = brunchService.brunchToBrunchInfoDTO(brunch);

      assertThat(result.getQuestions()).hasSize(2);
      Set<BrunchQuestionInfoDTO> questions = result.getQuestions();

      BrunchQuestionInfoDTO q1 =
          questions.stream().filter(q -> q.getId().equals(1)).findFirst().orElse(null);

      assertThat(q1).isNotNull();
      assertThat(q1.getTitle().get()).isEqualTo("Question 1");
      assertThat(q1.getMin()).isEqualTo(1);
      assertThat(q1.getMax()).isEqualTo(5);
      assertThat(q1.getOptional()).isFalse();
      assertThat(q1.getOrder()).isEqualTo(1);
      assertThat(q1.getLink().isPresent()).isTrue();
      assertThat(q1.getLink().get().toString()).isEqualTo("https://example.com");
    }
  }

  @Nested
  @DisplayName("Question to BrunchQuestionInfoDTO conversion")
  class QuestionToBrunchQuestionInfoDTOTests {

    @Test
    @DisplayName("Should convert question with all fields")
    void shouldConvertQuestionWithAllFields() throws MalformedURLException {
      Question question = new Question();
      question.setId(42);
      question.setTitle("Test Question");
      question.setMin(2);
      question.setMax(8);
      question.setOptional(true);
      question.setOrder(3);
      question.setRecommended(5);
      question.setLink(URI.create("https://test.example.com").toURL());

      BrunchQuestionInfoDTO result = brunchService.questionToBrunchQuestionInfoDTO(question);

      assertThat(result.getId()).isEqualTo(42);
      assertThat(result.getTitle().get()).isEqualTo("Test Question");
      assertThat(result.getMin()).isEqualTo(2);
      assertThat(result.getMax()).isEqualTo(8);
      assertThat(result.getOptional()).isTrue();
      assertThat(result.getOrder()).isEqualTo(3);
      assertThat(result.getRecommended()).isEqualTo(5);
      assertThat(result.getLink().isPresent()).isTrue();
      assertThat(result.getLink().get().toString()).isEqualTo("https://test.example.com");
    }

    @Test
    @DisplayName("Should use default values for null min/max")
    void shouldUseDefaultValuesForNullMinMax() {
      Question question = new Question();
      question.setId(1);
      question.setMin(null);
      question.setMax(null);
      question.setOptional(null);

      BrunchQuestionInfoDTO result = brunchService.questionToBrunchQuestionInfoDTO(question);

      assertThat(result.getMin()).isEqualTo(1);
      assertThat(result.getMax()).isEqualTo(5);
      assertThat(result.getOptional()).isFalse();
    }

    @Test
    @DisplayName("Should handle null link")
    void shouldHandleNullLink() {
      Question question = new Question();
      question.setId(1);
      question.setLink(null);

      BrunchQuestionInfoDTO result = brunchService.questionToBrunchQuestionInfoDTO(question);

      assertThat(result.getLink().isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should handle invalid URL in link")
    void shouldHandleInvalidUrlInLink() throws Exception {
      Question question = new Question();
      question.setId(1);

      // Create a URL that will be problematic during URI conversion
      // This simulates the URISyntaxException case in the service
      java.lang.reflect.Field linkField = Question.class.getDeclaredField("link");
      linkField.setAccessible(true);

      // Create a mock URL that will cause problems
      java.net.URL mockUrl = new java.net.URL("http://example.com/invalid path with spaces");
      linkField.set(question, mockUrl);

      BrunchQuestionInfoDTO result = brunchService.questionToBrunchQuestionInfoDTO(question);

      assertThat(result.getLink().isPresent()).isFalse();
    }
  }

  @Nested
  @DisplayName("BrunchCreateDTO to Brunch conversion")
  class BrunchCreateDTOToBrunchTests {

    @Test
    @DisplayName("Should convert BrunchCreateDTO to Brunch entity")
    void shouldConvertBrunchCreateDTOToBrunch() {
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("new-brunch");
      createDTO.setTitle("New Brunch");
      createDTO.setRequireEmail(true);
      createDTO.setEmailRegexp(JsonNullable.of(".*@company\\.com"));
      createDTO.setAdminPassword(JsonNullable.of("admin-pass"));
      createDTO.setVotingPassword(JsonNullable.of("vote-pass"));

      BrunchQuestionDTO questionDTO = new BrunchQuestionDTO();
      questionDTO.setId(1);
      questionDTO.setTitle(JsonNullable.of("Sample Question"));
      questionDTO.setMin(1);
      questionDTO.setMax(10);
      questionDTO.setOptional(false);
      questionDTO.setOrder(JsonNullable.of(1));
      questionDTO.setRecommended(JsonNullable.of(7));
      questionDTO.setLink(JsonNullable.of(URI.create("https://example.com/question")));

      createDTO.setQuestions(Set.of(questionDTO));

      Brunch result = brunchService.brunchCreateDTOToBrunch(createDTO);

      assertThat(result.getId()).isEqualTo("new-brunch");
      assertThat(result.getTitle()).isEqualTo("New Brunch");
      assertThat(result.getRequireEmail()).isTrue();
      assertThat(result.getEmailRegexp()).isEqualTo(".*@company\\.com");

      assertThat(result.getBrunchAuthorization()).isNotNull();
      assertThat(result.getBrunchAuthorization().getBrunchId()).isEqualTo("new-brunch");

      // Verify passwords are properly encoded with BCrypt
      BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
      assertThat(result.getBrunchAuthorization().getAdminPasswordHash()).startsWith("{bcrypt}");
      String adminHashWithoutPrefix =
          result.getBrunchAuthorization().getAdminPasswordHash().substring(8);
      assertThat(encoder.matches("admin-pass", adminHashWithoutPrefix)).isTrue();

      assertThat(result.getBrunchAuthorization().getVotingPasswordHash()).startsWith("{bcrypt}");
      String votingHashWithoutPrefix =
          result.getBrunchAuthorization().getVotingPasswordHash().substring(8);
      assertThat(encoder.matches("vote-pass", votingHashWithoutPrefix)).isTrue();

      assertThat(result.getQuestions()).hasSize(1);
      Question question = result.getQuestions().get(0);
      assertThat(question.getTitle()).isEqualTo("Sample Question");
      assertThat(question.getMin()).isEqualTo(1);
      assertThat(question.getMax()).isEqualTo(10);
      assertThat(question.getOptional()).isFalse();
      assertThat(question.getOrder()).isEqualTo(1);
      assertThat(question.getRecommended()).isEqualTo(7);
      assertThat(question.getBrunch()).isEqualTo(result);
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void shouldHandleNullOptionalFields() {
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("minimal-brunch");
      createDTO.setTitle("Minimal Brunch");
      createDTO.setRequireEmail(null);
      createDTO.setEmailRegexp(JsonNullable.undefined());
      createDTO.setAdminPassword(JsonNullable.undefined());
      createDTO.setVotingPassword(JsonNullable.undefined());
      createDTO.setQuestions(Set.of());

      Brunch result = brunchService.brunchCreateDTOToBrunch(createDTO);

      assertThat(result.getId()).isEqualTo("minimal-brunch");
      assertThat(result.getTitle()).isEqualTo("Minimal Brunch");
      assertThat(result.getRequireEmail()).isFalse();
      assertThat(result.getEmailRegexp()).isNull();

      assertThat(result.getBrunchAuthorization()).isNotNull();
      assertThat(result.getBrunchAuthorization().getAdminPasswordHash()).isNull();
      assertThat(result.getBrunchAuthorization().getVotingPasswordHash()).isNull();

      assertThat(result.getQuestions()).isEmpty();
    }

    @Test
    @DisplayName("Should sort questions by order")
    void shouldSortQuestionsByOrder() {
      BrunchCreateDTO createDTO = new BrunchCreateDTO();
      createDTO.setId("sorted-brunch");
      createDTO.setTitle("Sorted Questions");
      createDTO.setAdminPassword(JsonNullable.of("admin"));
      createDTO.setVotingPassword(JsonNullable.of("vote"));

      BrunchQuestionDTO q1 = new BrunchQuestionDTO();
      q1.setId(1);
      q1.setTitle(JsonNullable.of("Third Question"));
      q1.setOrder(JsonNullable.of(3));

      BrunchQuestionDTO q2 = new BrunchQuestionDTO();
      q2.setId(2);
      q2.setTitle(JsonNullable.of("First Question"));
      q2.setOrder(JsonNullable.of(1));

      BrunchQuestionDTO q3 = new BrunchQuestionDTO();
      q3.setId(3);
      q3.setTitle(JsonNullable.of("Second Question"));
      q3.setOrder(JsonNullable.of(2));

      BrunchQuestionDTO q4 = new BrunchQuestionDTO();
      q4.setId(4);
      q4.setTitle(JsonNullable.of("Last Question"));
      q4.setOrder(JsonNullable.undefined());

      createDTO.setQuestions(Set.of(q1, q2, q3, q4));

      Brunch result = brunchService.brunchCreateDTOToBrunch(createDTO);

      assertThat(result.getQuestions()).hasSize(4);
      List<Question> questions = result.getQuestions();

      assertThat(questions.get(0).getTitle()).isEqualTo("First Question");
      assertThat(questions.get(0).getOrder()).isEqualTo(1);

      assertThat(questions.get(1).getTitle()).isEqualTo("Second Question");
      assertThat(questions.get(1).getOrder()).isEqualTo(2);

      assertThat(questions.get(2).getTitle()).isEqualTo("Third Question");
      assertThat(questions.get(2).getOrder()).isEqualTo(3);

      assertThat(questions.get(3).getTitle()).isEqualTo("Last Question");
      assertThat(questions.get(3).getOrder()).isNull();
    }
  }
}
