package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.fon.bg.ac.rs.farma.domain.ProizvodnjaMleka;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProizvodnjaMlekaRepository extends JpaRepository<ProizvodnjaMleka, Long> {
    boolean existsByKravaIdAndDatum(Long kravaId, LocalDate datum);
    List<ProizvodnjaMleka> findByKravaIdAndDatumBetweenOrderByDatumAsc(Long kravaId, LocalDate od, LocalDate doDatuma);

    @Query("""
        select coalesce(sum(p.ukupnoLitara), 0)
        from ProizvodnjaMleka p
        where p.krava.farma.id = :farmaId and p.datum between :od and :doDatuma
        """)
    BigDecimal ukupnoZaFarmu(@Param("farmaId") Long farmaId,
                             @Param("od") LocalDate od,
                             @Param("doDatuma") LocalDate doDatuma);
}
