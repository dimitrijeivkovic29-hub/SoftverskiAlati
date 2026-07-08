package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatistikaServiceTest {

    @Mock private FarmaRepository farmaRepository;
    @Mock private KravaRepository kravaRepository;
    @Mock private BikRepository bikRepository;
    @Mock private VeterinarRepository veterinarRepository;
    @Mock private SteonostRepository steonostRepository;
    @Mock private ProizvodnjaMlekaRepository mlekoRepository;

    private StatistikaService service;

    @BeforeEach
    void setUp() {
        service = new StatistikaService(farmaRepository, kravaRepository, bikRepository,
                veterinarRepository, steonostRepository, mlekoRepository);
    }

    @Test
    void prikaziVracaAgregiranePodatke() {
        LocalDate od = LocalDate.of(2026, 6, 1);
        LocalDate doDatuma = LocalDate.of(2026, 6, 30);
        when(farmaRepository.findById(1L)).thenReturn(Optional.of(
                Farma.builder().id(1L).naziv("Zelena dolina").build()));
        for (StatusKrave status : StatusKrave.values()) {
            when(kravaRepository.countByFarmaIdAndStatus(1L, status))
                    .thenReturn(status == StatusKrave.U_LAKTACIJI ? 4L : 0L);
        }
        when(kravaRepository.countByFarmaId(1L)).thenReturn(6L);
        when(bikRepository.count()).thenReturn(2L);
        when(veterinarRepository.count()).thenReturn(3L);
        when(steonostRepository.countByKravaFarmaIdAndAktivnaTrue(1L)).thenReturn(1L);
        when(mlekoRepository.ukupnoZaFarmu(1L, od, doDatuma)).thenReturn(new BigDecimal("980.50"));

        var statistika = service.prikazi(1L, od, doDatuma);

        assertThat(statistika.nazivFarme()).isEqualTo("Zelena dolina");
        assertThat(statistika.brojKrava()).isEqualTo(6L);
        assertThat(statistika.brojKravaPoStatusu().get(StatusKrave.U_LAKTACIJI)).isEqualTo(4L);
        assertThat(statistika.brojBikova()).isEqualTo(2L);
        assertThat(statistika.brojVeterinara()).isEqualTo(3L);
        assertThat(statistika.brojAktivnihSteonosti()).isEqualTo(1L);
        assertThat(statistika.ukupnoMleka()).isEqualByComparingTo("980.50");
    }

    @Test
    void prikaziPretvaraNullUkupnoMlekaUNulu() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        when(farmaRepository.findById(1L)).thenReturn(Optional.of(
                Farma.builder().id(1L).naziv("Farma").build()));
        when(mlekoRepository.ukupnoZaFarmu(1L, datum, datum)).thenReturn(null);

        var statistika = service.prikazi(1L, datum, datum);

        assertThat(statistika.ukupnoMleka()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void prikaziOdbijaObrnutPeriod() {
        assertThatThrownBy(() -> service.prikazi(1L,
                LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(farmaRepository, kravaRepository, bikRepository,
                veterinarRepository, steonostRepository, mlekoRepository);
    }

    @Test
    void prikaziPrijavljujeNepostojecuFarmu() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        when(farmaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.prikazi(99L, datum, datum))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
