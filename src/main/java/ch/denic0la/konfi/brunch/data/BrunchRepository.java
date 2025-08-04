package ch.denic0la.konfi.brunch.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrunchRepository extends JpaRepository<Brunch, String> {
  @Query("select b.id from Brunch b")
  List<String> getAllBrunchIds();
}
