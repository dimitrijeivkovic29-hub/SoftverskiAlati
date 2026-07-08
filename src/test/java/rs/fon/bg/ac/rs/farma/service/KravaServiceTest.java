package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.domain.Krava;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.KravaRequest;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.FarmaRepository;
import rs.fon.bg.ac.rs.farma.repository.KravaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KravaServiceTest {

    @Mock
    private KravaRepository kravaRepository;
    @Mock
    private FarmaRepository farmaRepository;
    private KravaService service;
    private Farma farma;

    @BeforeEach
    void setUp() {
        service = new KravaService(kravaRepository, farmaRepository);
        farma = Farma.builder().id(10L).naziv("Zelena dolina").adresa("A").pib("123456789").build();
    }

    private KravaRequest request(String markica) {
        return new KravaRequest(markica, LocalDate.of(2023, 3, 10), "Holstajn", 1,
                StatusKrave.ZA_OSEMENJAVANJE, 10L);
    }

    @Test
    void dodajCuvaKravuIPovezujeFarmu() {
        when(kravaRepository.existsByBrojMarkice("RS-001")).thenReturn(false);
        when(farmaRepository.findById(10L)).thenReturn(Optional.of(farma));
        when(kravaRepository.save(any(Krava.class))).thenAnswer(invocation -> {
            Krava krava = invocation.getArgument(0);
            krava.setId(1L);
            return krava;
        });

        var response = service.dodaj(request("RS-001"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.farmaId()).isEqualTo(10L);
        assertThat(response.nazivFarme()).isEqualTo("Zelena dolina");
    }

    @Test
    void dodajOdbijaDupluMarkicu() {
        when(kravaRepository.existsByBrojMarkice("RS-001")).thenReturn(true);

        assertThatThrownBy(() -> service.dodaj(request("RS-001")))
                .isInstanceOf(DuplicateResourceException.class);
        verifyNoInteractions(farmaRepository);
        verify(kravaRepository, never()).save(any());
    }

    @Test
    void dodajPrijavljujeNepostojecuFarmu() {
        when(kravaRepository.existsByBrojMarkice("RS-001")).thenReturn(false);
        when(farmaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.dodaj(request("RS-001")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void izmeniMenjaPostojecuKravu() {
        Krava krava = Krava.builder().id(1L).brojMarkice("RS-OLD")
                .datumRodjenja(LocalDate.of(2022, 1, 1)).rasa("Simentalac")
                .laktacija(0).status(StatusKrave.JUNICA).farma(farma).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));
        when(kravaRepository.existsByBrojMarkiceAndIdNot("RS-NEW", 1L)).thenReturn(false);
        when(farmaRepository.findById(10L)).thenReturn(Optional.of(farma));
        when(kravaRepository.save(krava)).thenReturn(krava);

        var response = service.izmeni(1L, request("RS-NEW"));

        assertThat(response.brojMarkice()).isEqualTo("RS-NEW");
        assertThat(response.status()).isEqualTo(StatusKrave.ZA_OSEMENJAVANJE);
    }

    @Test
    void pretraziTrimujeBrojMarkiceIMapiraRezultat() {
        Krava krava = Krava.builder().id(1L).brojMarkice("RS-001")
                .datumRodjenja(LocalDate.of(2023, 3, 10)).rasa("Holstajn")
                .laktacija(1).status(StatusKrave.U_LAKTACIJI).farma(farma).build();
        when(kravaRepository.pretrazi("RS", StatusKrave.U_LAKTACIJI)).thenReturn(List.of(krava));

        var rezultat = service.pretrazi("  RS  ", StatusKrave.U_LAKTACIJI);

        assertThat(rezultat).hasSize(1);
        assertThat(rezultat.get(0).brojMarkice()).isEqualTo("RS-001");
        verify(kravaRepository).pretrazi("RS", StatusKrave.U_LAKTACIJI);
    }

    @Test
    void pretraziPrazanKriterijumPretvaraUNull() {
        when(kravaRepository.pretrazi(null, null)).thenReturn(List.of());

        assertThat(service.pretrazi("   ", null)).isEmpty();
        verify(kravaRepository).pretrazi(null, null);
    }

    @Test
    void obrisiPostojecuKravu() {
        Krava krava = Krava.builder().id(1L).farma(farma).build();
        when(kravaRepository.findById(1L)).thenReturn(Optional.of(krava));

        service.obrisi(1L);

        verify(kravaRepository).delete(krava);
    }
}
