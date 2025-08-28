package ch.denic0la.konfi.brunch.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrunchRepository extends JpaRepository<Brunch, String> {

  @Query("select b.id from Brunch b")
  List<String> getAllBrunchIds();

  @Query("SELECT b FROM Brunch b LEFT JOIN FETCH b.questions WHERE b.id = :id")
  Optional<Brunch> findByIdWithQuestions(@Param("id") String id);

  @Query(
      "SELECT b FROM Brunch b LEFT JOIN FETCH b.questions LEFT JOIN FETCH b.votes WHERE b.id = :id")
  Optional<Brunch> findByIdWithQuestionsAndVotes(@Param("id") String id);
}
