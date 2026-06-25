package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.fon.bg.ac.rs.farma.domain.Steonost;

import java.util.Optional;

public interface SteonostRepository extends JpaRepository<Steonost, Long> {
    Optional<Steonost> findFirstByKravaIdAndAktivnaTrue(Long kravaId);
    long countByKravaFarmaIdAndAktivnaTrue(Long farmaId);
}
