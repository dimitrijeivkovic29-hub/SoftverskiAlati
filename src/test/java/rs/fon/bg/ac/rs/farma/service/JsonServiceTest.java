package rs.fon.bg.ac.rs.farma.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonServiceTest {

    @Mock private FarmaRepository farmaRepository;
    @Mock private KravaRepository kravaRepository;
    @Mock private BikRepository bikRepository;
    @Mock private VeterinarRepository veterinarRepository;
    @Mock private OsemenjavanjeRepository osemenjavanjeRepository;
    @Mock private SteonostRepository steonostRepository;
    @Mock private TeljenjeRepository teljenjeRepository;
    @Mock private ProizvodnjaMlekaRepository mlekoRepository;

    private ObjectMapper objectMapper;
    private JsonService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        service = new JsonService(objectMapper, farmaRepository, kravaRepository, bikRepository,
                veterinarRepository, osemenjavanjeRepository, steonostRepository,
                teljenjeRepository, mlekoRepository);
    }

    @Test
    void izveziPraviValidanJsonSaVerzijomIListama() throws Exception {
        when(farmaRepository.findAll()).thenReturn(List.of(
                Farma.builder().id(1L).naziv("Zelena dolina").adresa("Glavna 1").pib("123456789").build()));
        when(kravaRepository.findAll()).thenReturn(List.of());
        when(bikRepository.findAll()).thenReturn(List.of());
        when(veterinarRepository.findAll()).thenReturn(List.of());
        when(osemenjavanjeRepository.findAll()).thenReturn(List.of());
        when(steonostRepository.findAll()).thenReturn(List.of());
        when(teljenjeRepository.findAll()).thenReturn(List.of());
        when(mlekoRepository.findAll()).thenReturn(List.of());

        byte[] json = service.izvezi();
        JsonNode root = objectMapper.readTree(json);

        assertThat(root.get("verzija").asInt()).isEqualTo(1);
        assertThat(root.get("farme").size()).isEqualTo(1);
        assertThat(root.get("farme").get(0).get("naziv").asText()).isEqualTo("Zelena dolina");
        assertThat(root.get("krave").isEmpty()).isTrue();
    }

    @Test
    void uveziMinimalniDokument() {
        byte[] json = "{\"verzija\":1}".getBytes(StandardCharsets.UTF_8);

        var rezultat = service.uvezi(json);

        assertThat(rezultat.farme()).isZero();
        assertThat(rezultat.krave()).isZero();
        assertThat(rezultat.proizvodnjaMleka()).isZero();
    }

    @Test
    void uveziJednuFarmu() {
        byte[] json = ("{\"verzija\":1,\"farme\":[{" +
                "\"id\":7,\"naziv\":\"Farma\",\"adresa\":\"Adresa\",\"pib\":\"123456789\"}]}")
                .getBytes(StandardCharsets.UTF_8);
        when(farmaRepository.save(any(Farma.class))).thenAnswer(invocation -> {
            Farma farma = invocation.getArgument(0);
            farma.setId(100L);
            return farma;
        });

        var rezultat = service.uvezi(json);

        assertThat(rezultat.farme()).isEqualTo(1);
        verify(farmaRepository).save(argThat((Farma f) ->
                f.getNaziv().equals("Farma") && f.getPib().equals("123456789")));
    }

    @Test
    void uveziOdbijaNeispravanJson() {
        assertThatThrownBy(() -> service.uvezi("nije-json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nije ispravan");
    }

    @Test
    void uveziOdbijaNepodrzanuVerziju() {
        assertThatThrownBy(() -> service.uvezi(
                "{\"verzija\":2}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nepodrzana verzija");
    }

    @Test
    void uveziOdbijaNepraznuBazu() {
        when(farmaRepository.count()).thenReturn(1L);

        assertThatThrownBy(() -> service.uvezi(
                "{\"verzija\":1}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("praznu bazu");
        verify(farmaRepository, never()).save(any());
    }
}
