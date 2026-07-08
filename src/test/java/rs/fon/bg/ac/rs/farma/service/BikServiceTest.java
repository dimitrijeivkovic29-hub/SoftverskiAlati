package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Bik;
import rs.fon.bg.ac.rs.farma.dto.BikRequest;
import rs.fon.bg.ac.rs.farma.dto.BikResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.BikRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BikServiceTest {

    @Mock
    private BikRepository repository;
    private BikService service;

    @BeforeEach
    void setUp() {
        service = new BikService(repository);
    }

    @Test
    void dodajCuvaBikaKadaHbBrojNijeZauzet() {
        BikRequest request = new BikRequest("Atlas", "HB-1", "Holstajn");
        when(repository.existsByHbBroj("HB-1")).thenReturn(false);
        when(repository.save(any(Bik.class))).thenAnswer(invocation -> {
            Bik bik = invocation.getArgument(0);
            bik.setId(1L);
            return bik;
        });

        BikResponse response = service.dodaj(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.hbBroj()).isEqualTo("HB-1");
        verify(repository).save(any(Bik.class));
    }

    @Test
    void dodajOdbijaDupliHbBroj() {
        when(repository.existsByHbBroj("HB-1")).thenReturn(true);

        assertThatThrownBy(() -> service.dodaj(new BikRequest("Atlas", "HB-1", "Holstajn")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("HB brojem");
        verify(repository, never()).save(any());
    }

    @Test
    void izmeniMenjaPostojeciEntitet() {
        Bik bik = Bik.builder().id(2L).naziv("Staro").hbBroj("HB-2").rasa("Rasa").build();
        when(repository.findById(2L)).thenReturn(Optional.of(bik));
        when(repository.existsByHbBrojAndIdNot("HB-3", 2L)).thenReturn(false);
        when(repository.save(bik)).thenReturn(bik);

        BikResponse response = service.izmeni(2L, new BikRequest("Novo", "HB-3", "Simentalac"));

        assertThat(response.naziv()).isEqualTo("Novo");
        assertThat(response.hbBroj()).isEqualTo("HB-3");
    }

    @Test
    void getEntityPrijavljujeNepostojeciEntitet() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void sviMapiraEntiteteUOdgovore() {
        when(repository.findAll()).thenReturn(List.of(
                Bik.builder().id(1L).naziv("Atlas").hbBroj("HB-1").rasa("Holstajn").build()));

        assertThat(service.svi()).singleElement().satisfies(response ->
                assertThat(response.naziv()).isEqualTo("Atlas"));
    }
}
