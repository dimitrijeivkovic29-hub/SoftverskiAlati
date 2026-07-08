package rs.fon.bg.ac.rs.farma.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Veterinar;
import rs.fon.bg.ac.rs.farma.dto.VeterinarRequest;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.VeterinarRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeterinarServiceTest {

    @Mock
    private VeterinarRepository repository;
    private VeterinarService service;

    @BeforeEach
    void setUp() {
        service = new VeterinarService(repository);
    }

    @Test
    void dodajCuvaVeterinara() {
        when(repository.existsByTelefon("060123456")).thenReturn(false);
        when(repository.save(any(Veterinar.class))).thenAnswer(invocation -> {
            Veterinar veterinar = invocation.getArgument(0);
            veterinar.setId(1L);
            return veterinar;
        });

        var response = service.dodaj(new VeterinarRequest("Ana", "Ilic", "060123456"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.ime()).isEqualTo("Ana");
    }

    @Test
    void dodajOdbijaDupliTelefon() {
        when(repository.existsByTelefon("060123456")).thenReturn(true);

        assertThatThrownBy(() -> service.dodaj(
                new VeterinarRequest("Ana", "Ilic", "060123456")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void sviMapiraListu() {
        when(repository.findAll()).thenReturn(List.of(
                Veterinar.builder().id(1L).ime("Ana").prezime("Ilic").telefon("060123456").build()));

        assertThat(service.svi()).singleElement().satisfies(response ->
                assertThat(response.prezime()).isEqualTo("Ilic"));
    }

    @Test
    void getEntityPrijavljujeNepostojeciEntitet() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntity(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("7");
    }
}
