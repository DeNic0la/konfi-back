package ch.denic0la.konfi.brunch.data;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BrunchRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private BrunchRepository brunchRepository;

  @BeforeEach
  void setUp() {
    entityManager.clear();
  }

  @Test
  @DisplayName("Should save and retrieve brunch")
  void shouldSaveAndRetrieveBrunch() {
    Brunch brunch = createSampleBrunch("save-test", "Save Test Brunch");

    Brunch saved = brunchRepository.save(brunch);
    entityManager.flush();

    assertThat(saved.getId()).isEqualTo("save-test");
    assertThat(saved.getTitle()).isEqualTo("Save Test Brunch");

    Optional<Brunch> retrieved = brunchRepository.findById("save-test");
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getTitle()).isEqualTo("Save Test Brunch");
  }

  @Test
  @DisplayName("Should save brunch with questions and cascade operations")
  void shouldSaveBrunchWithQuestionsAndCascade() throws MalformedURLException {
    Brunch brunch = createSampleBrunch("cascade-test", "Cascade Test");

    Question question1 = createSampleQuestion("Question 1", brunch, 1, 1, 5);
    question1.setLink(URI.create("https://example.com/q1").toURL());

    Question question2 = createSampleQuestion("Question 2", brunch, 2, 0, 10);
    question2.setOptional(true);

    brunch.setQuestions(List.of(question1, question2));

    Brunch saved = brunchRepository.save(brunch);
    entityManager.flush();

    Optional<Brunch> retrieved = brunchRepository.findById("cascade-test");
    assertThat(retrieved).isPresent();

    Brunch retrievedBrunch = retrieved.get();
    assertThat(retrievedBrunch.getQuestions()).hasSize(2);

    List<Question> questions = retrievedBrunch.getQuestions();
    questions.sort((q1, q2) -> Integer.compare(q1.getOrder(), q2.getOrder()));

    Question q1 = questions.get(0);
    assertThat(q1.getTitle()).isEqualTo("Question 1");
    assertThat(q1.getMin()).isEqualTo(1);
    assertThat(q1.getMax()).isEqualTo(5);
    assertThat(q1.getOptional()).isFalse();
    assertThat(q1.getLink().toString()).isEqualTo("https://example.com/q1");

    Question q2 = questions.get(1);
    assertThat(q2.getTitle()).isEqualTo("Question 2");
    assertThat(q2.getMin()).isEqualTo(0);
    assertThat(q2.getMax()).isEqualTo(10);
    assertThat(q2.getOptional()).isTrue();
  }

  @Test
  @DisplayName("Should get all brunch IDs using custom query")
  void shouldGetAllBrunchIdsUsingCustomQuery() {
    Brunch brunch1 = createSampleBrunch("query-test-1", "Query Test 1");
    Brunch brunch2 = createSampleBrunch("query-test-2", "Query Test 2");
    Brunch brunch3 = createSampleBrunch("query-test-3", "Query Test 3");

    brunchRepository.saveAll(List.of(brunch1, brunch2, brunch3));
    entityManager.flush();

    List<String> brunchIds = brunchRepository.getAllBrunchIds();

    assertThat(brunchIds)
        .hasSize(3)
        .containsExactlyInAnyOrder("query-test-1", "query-test-2", "query-test-3");
  }

  @Test
  @DisplayName("Should delete brunch and cascade to related entities")
  void shouldDeleteBrunchAndCascadeToRelatedEntities() {
    Brunch brunch = createSampleBrunch("delete-test", "Delete Test");

    Question question = createSampleQuestion("Delete Question", brunch, 1, 1, 5);
    brunch.setQuestions(List.of(question));

    BrunchAuthorization auth =
        BrunchAuthorization.builder()
            .brunch(brunch)
            .brunchId("delete-test")
            .adminPasswordHash("admin-hash")
            .votingPasswordHash("vote-hash")
            .build();
    brunch.setBrunchAuthorization(auth);

    brunchRepository.save(brunch);
    entityManager.flush();

    assertThat(brunchRepository.findById("delete-test")).isPresent();

    brunchRepository.deleteById("delete-test");
    entityManager.flush();

    assertThat(brunchRepository.findById("delete-test")).isEmpty();

    // Verify related entities are also deleted due to cascade
    assertThat(
            entityManager
                .getEntityManager()
                .createQuery("SELECT q FROM Question q WHERE q.brunch.id = :brunchId")
                .setParameter("brunchId", "delete-test")
                .getResultList())
        .isEmpty();
  }

  @Test
  @DisplayName("Should handle brunch with email requirements")
  void shouldHandleBrunchWithEmailRequirements() {
    Brunch brunch =
        Brunch.builder()
            .id("email-test")
            .title("Email Test Brunch")
            .requireEmail(true)
            .emailRegexp(".*@company\\.com")
            .questions(new ArrayList<>())
            .votes(new ArrayList<>())
            .build();

    brunchRepository.save(brunch);
    entityManager.flush();

    Optional<Brunch> retrieved = brunchRepository.findById("email-test");
    assertThat(retrieved).isPresent();

    Brunch retrievedBrunch = retrieved.get();
    assertThat(retrievedBrunch.getRequireEmail()).isTrue();
    assertThat(retrievedBrunch.getEmailRegexp()).isEqualTo(".*@company\\.com");
  }

  @Test
  @DisplayName("Should handle brunch without email requirements")
  void shouldHandleBrunchWithoutEmailRequirements() {
    Brunch brunch =
        Brunch.builder()
            .id("no-email-test")
            .title("No Email Test")
            .requireEmail(false)
            .emailRegexp(null)
            .questions(new ArrayList<>())
            .votes(new ArrayList<>())
            .build();

    brunchRepository.save(brunch);
    entityManager.flush();

    Optional<Brunch> retrieved = brunchRepository.findById("no-email-test");
    assertThat(retrieved).isPresent();

    Brunch retrievedBrunch = retrieved.get();
    assertThat(retrievedBrunch.getRequireEmail()).isFalse();
    assertThat(retrievedBrunch.getEmailRegexp()).isNull();
  }

  @Test
  @DisplayName("Should return empty list for getAllBrunchIds when no brunches exist")
  void shouldReturnEmptyListWhenNoBrunchesExist() {
    List<String> brunchIds = brunchRepository.getAllBrunchIds();
    assertThat(brunchIds).isEmpty();
  }

  @Test
  @DisplayName("Should handle questions ordered correctly")
  void shouldHandleQuestionsOrderedCorrectly() {
    Brunch brunch = createSampleBrunch("order-test", "Order Test");

    Question q3 = createSampleQuestion("Third Question", brunch, 3, 1, 5);
    Question q1 = createSampleQuestion("First Question", brunch, 1, 1, 5);
    Question q2 = createSampleQuestion("Second Question", brunch, 2, 1, 5);

    // Add in random order
    brunch.setQuestions(List.of(q3, q1, q2));

    brunchRepository.save(brunch);
    entityManager.flush();
    entityManager.clear(); // Clear the persistence context to ensure fresh fetch

    Optional<Brunch> retrieved = brunchRepository.findById("order-test");
    assertThat(retrieved).isPresent();

    List<Question> questions = retrieved.get().getQuestions();
    // Questions should be ordered by @OrderBy("order ASC")
    assertThat(questions).hasSize(3);

    // Check that questions are ordered by their order field value
    assertThat(questions.get(0).getOrder()).isEqualTo(1);
    assertThat(questions.get(0).getTitle()).isEqualTo("First Question");

    assertThat(questions.get(1).getOrder()).isEqualTo(2);
    assertThat(questions.get(1).getTitle()).isEqualTo("Second Question");

    assertThat(questions.get(2).getOrder()).isEqualTo(3);
    assertThat(questions.get(2).getTitle()).isEqualTo("Third Question");
  }

  private Brunch createSampleBrunch(String id, String title) {
    return Brunch.builder()
        .id(id)
        .title(title)
        .requireEmail(false)
        .questions(new ArrayList<>())
        .votes(new ArrayList<>())
        .build();
  }

  private Question createSampleQuestion(String title, Brunch brunch, int order, int min, int max) {
    Question question = new Question();
    question.setTitle(title);
    question.setBrunch(brunch);
    question.setOrder(order);
    question.setMin(min);
    question.setMax(max);
    question.setOptional(false);
    return question;
  }
}
