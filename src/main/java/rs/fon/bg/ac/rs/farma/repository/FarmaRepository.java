package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.fon.bg.ac.rs.farma.domain.Farma;

public interface FarmaRepository extends JpaRepository<Farma, Long> {
    boolean existsByPib(String pib);
    boolean existsByPibAndIdNot(String pib, Long id);
}
