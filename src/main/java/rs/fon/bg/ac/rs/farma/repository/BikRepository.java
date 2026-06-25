package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.fon.bg.ac.rs.farma.domain.Bik;

public interface BikRepository extends JpaRepository<Bik, Long> {
    boolean existsByHbBroj(String hbBroj);
    boolean existsByHbBrojAndIdNot(String hbBroj, Long id);
}
