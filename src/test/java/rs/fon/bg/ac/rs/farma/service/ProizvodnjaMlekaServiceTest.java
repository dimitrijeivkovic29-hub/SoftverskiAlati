package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Krava;
import rs.fon.bg.ac.rs.farma.domain.ProizvodnjaMleka;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.ProizvodnjaMlekaRequest;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.KravaRepository;
import rs.fon.bg.ac.rs.farma.repository.ProizvodnjaMlekaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProizvodnjaMlekaServiceTest {

    @Mock
    private ProizvodnjaMlekaRepository repository;
    @Mock
    private KravaRepository kravaRepository;
    private ProizvodnjaMlekaService service;
    private Krava krava;

    @BeforeEach
    void setUp() {
        service = new ProizvodnjaMlekaService(repository, kravaRepository);
        krava = Krava.builder().id(1L).brojMarkice("RS-001").status(StatusKrave.U_LAKTACIJI).build();
    }

    @Test
    void unesiRacunaUkupnuKolicinuICuvaUnos() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(repository.existsByKravaIdAndDatum(1L, datum)).thenReturn(false);
        when(repository.save(any(ProizvodnjaMleka.class))).thenAnswer(invocation -> {
            ProizvodnjaMleka p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        var response = service.unesi(new ProizvodnjaMlekaRequest(
                1L, datum, new BigDecimal("12.50"), new BigDecimal("10.25")));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ukupnoLitara()).isEqualByComparingTo("22.75");
    }

    @Test
    void unesiOdbijaIzlucenuKravu() {
        krava.setStatus(StatusKrave.IZLUCENA);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));

        assertThatThrownBy(() -> service.unesi(new ProizvodnjaMlekaRequest(
                1L, LocalDate.now(), BigDecimal.ONE, BigDecimal.ONE)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("izlucenu");
        verifyNoInteractions(repository);
    }

    @Test
    void unesiOdbijaDupliDatum() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(repository.existsByKravaIdAndDatum(1L, datum)).thenReturn(true);

        assertThatThrownBy(() -> service.unesi(new ProizvodnjaMlekaRequest(
                1L, datum, BigDecimal.ONE, BigDecimal.ONE)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void pregledRacunaUkupnoIProsek() {
        LocalDate od = LocalDate.of(2026, 6, 1);
        LocalDate doDatuma = LocalDate.of(2026, 6, 2);
        ProizvodnjaMleka prvi = ProizvodnjaMleka.builder().id(1L).krava(krava).datum(od)
                .jutarnjaLitara(new BigDecimal("10.00")).vecernjaLitara(new BigDecimal("8.00"))
                .ukupnoLitara(new BigDecimal("18.00")).build();
        ProizvodnjaMleka drugi = ProizvodnjaMleka.builder().id(2L).krava(krava).datum(doDatuma)
                .jutarnjaLitara(new BigDecimal("11.00")).vecernjaLitara(new BigDecimal("9.00"))
                .ukupnoLitara(new BigDecimal("20.00")).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(repository.findByKravaIdAndDatumBetweenOrderByDatumAsc(1L, od, doDatuma))
                .thenReturn(List.of(prvi, drugi));

        var izvestaj = service.pregled(1L, od, doDatuma);

        assertThat(izvestaj.ukupnoLitara()).isEqualByComparingTo("38.00");
        assertThat(izvestaj.prosekPoDanu()).isEqualByComparingTo("19.00");
        assertThat(izvestaj.stavke()).hasSize(2);
    }

    @Test
    void pregledPraznogPeriodaVracaNule() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(repository.findByKravaIdAndDatumBetweenOrderByDatumAsc(1L, datum, datum))
                .thenReturn(List.of());

        var izvestaj = service.pregled(1L, datum, datum);

        assertThat(izvestaj.ukupnoLitara()).isEqualByComparingTo("0.00");
        assertThat(izvestaj.prosekPoDanu()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void pregledOdbijaObrnutPeriod() {
        assertThatThrownBy(() -> service.pregled(1L,
                LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(kravaRepository, repository);
    }

    @Test
    void unesiPrijavljujeNepostojecuKravu() {
        when(kravaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unesi(new ProizvodnjaMlekaRequest(
                99L, LocalDate.now(), BigDecimal.ONE, BigDecimal.ONE)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
