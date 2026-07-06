package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.dto.FarmaRequest;
import rs.fon.bg.ac.rs.farma.dto.FarmaResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.FarmaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmaServiceTest {

    @Mock
    private FarmaRepository repository;
    private FarmaService service;

    @BeforeEach
    void setUp() {
        service = new FarmaService(repository);
    }

    @Test
    void dodajCuvaFarmu() {
        FarmaRequest request = new FarmaRequest("Zelena dolina", "Glavna 1", "123456789");
        when(repository.existsByPib("123456789")).thenReturn(false);
        when(repository.save(any(Farma.class))).thenAnswer(invocation -> {
            Farma farma = invocation.getArgument(0);
            farma.setId(1L);
            return farma;
        });

        FarmaResponse response = service.dodaj(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.naziv()).isEqualTo("Zelena dolina");
    }

    @Test
    void dodajOdbijaDupliPib() {
        when(repository.existsByPib("123456789")).thenReturn(true);

        assertThatThrownBy(() -> service.dodaj(
                new FarmaRequest("Farma", "Adresa", "123456789")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void nadjiVracaPostojecuFarmu() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                Farma.builder().id(1L).naziv("Farma").adresa("Adresa").pib("123456789").build()));

        assertThat(service.nadji(1L).pib()).isEqualTo("123456789");
    }

    @Test
    void izmeniOdbijaPibKojiPripadaDrugojFarmi() {
        Farma farma = Farma.builder().id(1L).naziv("Farma").adresa("Adresa").pib("123456789").build();
        when(repository.findById(1L)).thenReturn(Optional.of(farma));
        when(repository.existsByPibAndIdNot("987654321", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.izmeni(1L,
                new FarmaRequest("Nova", "Nova adresa", "987654321")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void obrisiPostojecuFarmu() {
        Farma farma = Farma.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(farma));

        service.obrisi(1L);

        verify(repository).delete(farma);
    }

    @Test
    void sveMapiraListu() {
        when(repository.findAll()).thenReturn(List.of(
                Farma.builder().id(1L).naziv("Farma").adresa("A").pib("123456789").build()));

        assertThat(service.sve()).extracting(FarmaResponse::naziv).containsExactly("Farma");
    }

    @Test
    void nadjiPrijavljujeNepostojecuFarmu() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.nadji(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("5");
    }
}
