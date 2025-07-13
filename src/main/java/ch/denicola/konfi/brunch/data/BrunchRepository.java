package ch.denicola.konfi.brunch.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrunchRepository extends JpaRepository<Brunch, String> {
}