package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.fon.bg.ac.rs.farma.domain.Osemenjavanje;

import java.util.List;
import java.util.Optional;

public interface OsemenjavanjeRepository extends JpaRepository<Osemenjavanje, Long> {
    long countByKravaId(Long kravaId);
    List<Osemenjavanje> findByKravaIdOrderByDatumDescRedniBrojDesc(Long kravaId);
    Optional<Osemenjavanje> findTopByKravaIdOrderByDatumDescRedniBrojDesc(Long kravaId);
}
