package ch.denicola.konfi.brunch.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrunchRepository extends JpaRepository<Brunch, String> {
    @Query("select b.id from Brunch b")
    List<String> getAllBrunchIds();
}