package ch.denic0la.konfi.brunch.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Integer> {

  List<Vote> findByBrunchId(String brunchId);

  Optional<Vote> findByBrunchIdAndName(String brunchId, String name);

  boolean existsByBrunchIdAndName(String brunchId, String name);

  long countByBrunchId(String brunchId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Vote v WHERE v.brunch.id = :brunchId")
  void deleteByBrunchId(@Param("brunchId") String brunchId);
}
