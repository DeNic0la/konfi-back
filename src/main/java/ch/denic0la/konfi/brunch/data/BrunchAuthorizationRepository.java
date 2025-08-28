package ch.denic0la.konfi.brunch.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface BrunchAuthorizationRepository extends JpaRepository<BrunchAuthorization, String> {

  @Query("select b from BrunchAuthorization b where b.brunchId = ?1")
  Optional<BrunchAuthorization> findByBrunchId(@NonNull String brunchId);
}
