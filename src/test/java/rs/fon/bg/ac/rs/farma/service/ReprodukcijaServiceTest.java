package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.*;
import rs.fon.bg.ac.rs.farma.dto.OsemenjavanjeRequest;
import rs.fon.bg.ac.rs.farma.dto.PotvrdaSteonostiRequest;
import rs.fon.bg.ac.rs.farma.dto.TeljenjeRequest;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReprodukcijaServiceTest {

    @Mock private KravaRepository kravaRepository;
    @Mock private BikRepository bikRepository;
    @Mock private VeterinarRepository veterinarRepository;
    @Mock private OsemenjavanjeRepository osemenjavanjeRepository;
    @Mock private SteonostRepository steonostRepository;
    @Mock private TeljenjeRepository teljenjeRepository;

    private ReprodukcijaService service;
    private Krava krava;
    private Bik bik;
    private Veterinar veterinar;

    @BeforeEach
    void setUp() {
        service = new ReprodukcijaService(kravaRepository, bikRepository, veterinarRepository,
                osemenjavanjeRepository, steonostRepository, teljenjeRepository);
        krava = Krava.builder().id(1L).brojMarkice("RS-001")
                .datumRodjenja(LocalDate.of(2023, 1, 1)).laktacija(1)
                .status(StatusKrave.ZA_OSEMENJAVANJE).build();
        bik = Bik.builder().id(2L).naziv("Atlas").hbBroj("HB-1").build();
        veterinar = Veterinar.builder().id(3L).ime("Ana").prezime("Ilic").build();
    }

    @Test
    void evidentirajOsemenjavanjeCuvaPostupakIPostavljaStatus() {
        LocalDate datum = LocalDate.of(2026, 5, 1);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(bikRepository.findById(2L)).thenReturn(Optional.of(bik));
        when(veterinarRepository.findById(3L)).thenReturn(Optional.of(veterinar));
        when(osemenjavanjeRepository.countByKravaId(1L)).thenReturn(1L);
        when(osemenjavanjeRepository.save(any(Osemenjavanje.class))).thenAnswer(invocation -> {
            Osemenjavanje o = invocation.getArgument(0);
            o.setId(10L);
            return o;
        });

        var response = service.evidentirajOsemenjavanje(
                new OsemenjavanjeRequest(1L, 2L, 3L, datum, "Drugi pokusaj"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.redniBroj()).isEqualTo(2);
        assertThat(response.bik()).isEqualTo("Atlas");
        assertThat(krava.getStatus()).isEqualTo(StatusKrave.ZA_PROVERU_STEONOSTI);
        verify(kravaRepository).save(krava);
    }

    @Test
    void evidentirajOsemenjavanjeOdbijaIzlucenuKravu() {
        krava.setStatus(StatusKrave.IZLUCENA);
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));

        assertThatThrownBy(() -> service.evidentirajOsemenjavanje(
                new OsemenjavanjeRequest(1L, 2L, 3L, LocalDate.now(), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Izlucena");
        verifyNoInteractions(bikRepository, veterinarRepository, osemenjavanjeRepository);
    }

    @Test
    void evidentirajOsemenjavanjeOdbijaDatumPreRodjenja() {
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));

        assertThatThrownBy(() -> service.evidentirajOsemenjavanje(
                new OsemenjavanjeRequest(1L, 2L, 3L, LocalDate.of(2022, 12, 31), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pre rodjenja");
    }

    @Test
    void istorijaOsemenjavanjaMapiraRezultate() {
        Osemenjavanje o = Osemenjavanje.builder().id(5L).datum(LocalDate.of(2026, 5, 1))
                .redniBroj(1).krava(krava).bik(bik).veterinar(veterinar).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(osemenjavanjeRepository.findByKravaIdOrderByDatumDescRedniBrojDesc(1L))
                .thenReturn(List.of(o));

        assertThat(service.istorijaOsemenjavanja(1L)).singleElement().satisfies(response -> {
            assertThat(response.brojMarkice()).isEqualTo("RS-001");
            assertThat(response.veterinar()).isEqualTo("Ana Ilic");
        });
    }

    @Test
    void kraveZaProveruFiltriraPoBrojuDana() {
        Krava spremna = Krava.builder().id(1L).brojMarkice("RS-001").build();
        Krava prerano = Krava.builder().id(2L).brojMarkice("RS-002").build();
        Osemenjavanje starije = Osemenjavanje.builder().datum(LocalDate.now().minusDays(35)).build();
        Osemenjavanje novije = Osemenjavanje.builder().datum(LocalDate.now().minusDays(10)).build();
        when(kravaRepository.findByStatus(StatusKrave.ZA_PROVERU_STEONOSTI))
                .thenReturn(List.of(spremna, prerano));
        when(osemenjavanjeRepository.findTopByKravaIdOrderByDatumDescRedniBrojDesc(1L))
                .thenReturn(Optional.of(starije));
        when(osemenjavanjeRepository.findTopByKravaIdOrderByDatumDescRedniBrojDesc(2L))
                .thenReturn(Optional.of(novije));

        var rezultat = service.kraveZaProveru(28);

        assertThat(rezultat).hasSize(1);
        assertThat(rezultat.get(0).kravaId()).isEqualTo(1L);
        assertThat(rezultat.get(0).danaOdOsemenjavanja()).isGreaterThanOrEqualTo(35);
    }

    @Test
    void kraveZaProveruOdbijaNeispravanMinimum() {
        assertThatThrownBy(() -> service.kraveZaProveru(0))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(kravaRepository, osemenjavanjeRepository);
    }

    @Test
    void potvrdiSteonostCuvaAktivnuSteonostIRacunaTermin() {
        LocalDate osemenjavanjeDatum = LocalDate.of(2026, 5, 1);
        LocalDate potvrda = LocalDate.of(2026, 5, 30);
        Osemenjavanje poslednje = Osemenjavanje.builder().datum(osemenjavanjeDatum).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(steonostRepository.findFirstByKravaIdAndAktivnaTrue(1L)).thenReturn(Optional.empty());
        when(osemenjavanjeRepository.findTopByKravaIdOrderByDatumDescRedniBrojDesc(1L))
                .thenReturn(Optional.of(poslednje));
        when(veterinarRepository.findById(3L)).thenReturn(Optional.of(veterinar));
        when(steonostRepository.save(any(Steonost.class))).thenAnswer(invocation -> {
            Steonost s = invocation.getArgument(0);
            s.setId(20L);
            return s;
        });

        var response = service.potvrdiSteonost(new PotvrdaSteonostiRequest(1L, 3L, potvrda));

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.ocekivaniDatumTeljenja()).isEqualTo(osemenjavanjeDatum.plusDays(280));
        assertThat(response.aktivna()).isTrue();
        assertThat(krava.getStatus()).isEqualTo(StatusKrave.STEONA);
    }

    @Test
    void potvrdiSteonostOdbijaPostojecuAktivnuSteonost() {
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(steonostRepository.findFirstByKravaIdAndAktivnaTrue(1L))
                .thenReturn(Optional.of(new Steonost()));

        assertThatThrownBy(() -> service.potvrdiSteonost(
                new PotvrdaSteonostiRequest(1L, 3L, LocalDate.now())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aktivnu steonost");
    }

    @Test
    void potvrdiSteonostOdbijaPotvrduPreOsemenjavanja() {
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(steonostRepository.findFirstByKravaIdAndAktivnaTrue(1L)).thenReturn(Optional.empty());
        when(osemenjavanjeRepository.findTopByKravaIdOrderByDatumDescRedniBrojDesc(1L))
                .thenReturn(Optional.of(Osemenjavanje.builder().datum(LocalDate.of(2026, 5, 10)).build()));

        assertThatThrownBy(() -> service.potvrdiSteonost(
                new PotvrdaSteonostiRequest(1L, 3L, LocalDate.of(2026, 5, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pre osemenjavanja");
    }

    @Test
    void evidentirajTeljenjeZatvaraSteonostIPovecavaLaktaciju() {
        Steonost steonost = Steonost.builder().id(30L).datumPotvrde(LocalDate.of(2026, 5, 30))
                .aktivna(true).krava(krava).veterinar(veterinar).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(steonostRepository.findFirstByKravaIdAndAktivnaTrue(1L)).thenReturn(Optional.of(steonost));
        when(teljenjeRepository.save(any(Teljenje.class))).thenAnswer(invocation -> {
            Teljenje t = invocation.getArgument(0);
            t.setId(40L);
            return t;
        });

        var response = service.evidentirajTeljenje(
                new TeljenjeRequest(1L, LocalDate.of(2026, 12, 1), 1, "Bez komplikacija"));

        assertThat(response.id()).isEqualTo(40L);
        assertThat(steonost.isAktivna()).isFalse();
        assertThat(krava.getLaktacija()).isEqualTo(2);
        assertThat(krava.getStatus()).isEqualTo(StatusKrave.U_LAKTACIJI);
        verify(steonostRepository).save(steonost);
        verify(kravaRepository).save(krava);
    }

    @Test
    void evidentirajTeljenjeOdbijaKravuBezAktivneSteonosti() {
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(steonostRepository.findFirstByKravaIdAndAktivnaTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evidentirajTeljenje(
                new TeljenjeRequest(1L, LocalDate.now(), 1, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nema aktivnu");
    }

    @Test
    void osemenjavanjePrijavljujeNepostojeciBik() {
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(bikRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evidentirajOsemenjavanje(
                new OsemenjavanjeRequest(1L, 99L, 3L, LocalDate.now(), null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
