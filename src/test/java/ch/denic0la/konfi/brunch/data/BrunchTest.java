package ch.denic0la.konfi.brunch.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrunchTest {

  @Test
  @DisplayName("Should create Brunch using builder")
  void shouldCreateBrunchUsingBuilder() {
    Brunch brunch =
        Brunch.builder()
            .id("test-id")
            .title("Test Brunch")
            .requireEmail(true)
            .emailRegexp(".*@test\\.com")
            .questions(new ArrayList<>())
            .votes(new ArrayList<>())
            .build();

    assertThat(brunch.getId()).isEqualTo("test-id");
    assertThat(brunch.getTitle()).isEqualTo("Test Brunch");
    assertThat(brunch.getRequireEmail()).isTrue();
    assertThat(brunch.getEmailRegexp()).isEqualTo(".*@test\\.com");
    assertThat(brunch.getQuestions()).isEmpty();
    assertThat(brunch.getVotes()).isEmpty();
  }

  @Test
  @DisplayName("Should create Brunch with all args constructor")
  void shouldCreateBrunchWithAllArgsConstructor() {
    List<Question> questions = new ArrayList<>();
    List<Vote> votes = new ArrayList<>();
    BrunchAuthorization auth = new BrunchAuthorization();

    Brunch brunch =
        new Brunch("all-args-id", "All Args Brunch", false, null, questions, votes, auth);

    assertThat(brunch.getId()).isEqualTo("all-args-id");
    assertThat(brunch.getTitle()).isEqualTo("All Args Brunch");
    assertThat(brunch.getRequireEmail()).isFalse();
    assertThat(brunch.getEmailRegexp()).isNull();
    assertThat(brunch.getQuestions()).isEqualTo(questions);
    assertThat(brunch.getVotes()).isEqualTo(votes);
    assertThat(brunch.getBrunchAuthorization()).isEqualTo(auth);
  }

  @Test
  @DisplayName("Should support setters and getters")
  void shouldSupportSettersAndGetters() {
    Brunch brunch = new Brunch();
    List<Question> questions = List.of();
    BrunchAuthorization auth = new BrunchAuthorization();

    brunch.setId("setter-id");
    brunch.setTitle("Setter Test");
    brunch.setRequireEmail(true);
    brunch.setEmailRegexp("setter@.*");
    brunch.setQuestions(questions);
    brunch.setBrunchAuthorization(auth);

    assertThat(brunch.getId()).isEqualTo("setter-id");
    assertThat(brunch.getTitle()).isEqualTo("Setter Test");
    assertThat(brunch.getRequireEmail()).isTrue();
    assertThat(brunch.getEmailRegexp()).isEqualTo("setter@.*");
    assertThat(brunch.getQuestions()).isEqualTo(questions);
    assertThat(brunch.getBrunchAuthorization()).isEqualTo(auth);
  }

  @Test
  @DisplayName("Should have correct table and column constants")
  void shouldHaveCorrectTableAndColumnConstants() {
    assertThat(Brunch.TABLE_NAME).isEqualTo("brunch");
    assertThat(Brunch.COLUMN_ID_NAME).isEqualTo("id");
    assertThat(Brunch.COLUMN_TITLE_NAME).isEqualTo("title");
    assertThat(Brunch.COLUMN_REQUIREEMAIL_NAME).isEqualTo("require_email");
    assertThat(Brunch.COLUMN_EMAILREGEXP_NAME).isEqualTo("email_regexp");
  }

  @Test
  @DisplayName("Should generate toString representation")
  void shouldGenerateToStringRepresentation() {
    Brunch brunch =
        Brunch.builder()
            .id("toString-test")
            .title("ToString Test Brunch")
            .requireEmail(false)
            .emailRegexp("test.*")
            .questions(new ArrayList<>())
            .votes(new ArrayList<>())
            .build();

    String toStringResult = brunch.toString();

    assertThat(toStringResult)
        .contains("toString-test")
        .contains("ToString Test Brunch")
        .contains("false")
        .contains("test.*");
  }

  @Test
  @DisplayName("Should support equality comparison")
  void shouldSupportEqualityComparison() {
    Brunch brunch1 =
        Brunch.builder().id("equality-test").title("Equality Test").requireEmail(true).build();

    Brunch brunch2 =
        Brunch.builder().id("equality-test").title("Equality Test").requireEmail(true).build();

    Brunch brunch3 =
        Brunch.builder().id("different-id").title("Equality Test").requireEmail(true).build();

    assertThat(brunch1).isEqualTo(brunch2);
    assertThat(brunch1).isNotEqualTo(brunch3);
    assertThat(brunch1.hashCode()).isEqualTo(brunch2.hashCode());
  }
}
