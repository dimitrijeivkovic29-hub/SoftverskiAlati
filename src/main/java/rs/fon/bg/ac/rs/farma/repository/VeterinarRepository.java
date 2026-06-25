package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.fon.bg.ac.rs.farma.domain.Veterinar;

public interface VeterinarRepository extends JpaRepository<Veterinar, Long> {
    boolean existsByTelefon(String telefon);
}
