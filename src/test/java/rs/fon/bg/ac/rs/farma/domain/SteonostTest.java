package rs.fon.bg.ac.rs.farma.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SteonostTest {

    private Validator validator;
    private Krava krava;
    private Veterinar veterinar;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        krava = Krava.builder().id(1L).build();
        veterinar = Veterinar.builder().id(2L).build();
    }

    @Test
    @DisplayName("Ispravna steonost prolazi validaciju")
    void validnaSteonostNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Steonost>> violations = validator.validate(validnaSteonost());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder pravilno postavlja datume, aktivnost i relacije")
    void builderPostavljaDatumeAktivnostIRelacije() {
        LocalDate datumPotvrde = LocalDate.of(2026, 5, 30);
        LocalDate ocekivaniDatum = LocalDate.of(2027, 2, 5);
        Steonost steonost = Steonost.builder()
                .id(3L)
                .datumPotvrde(datumPotvrde)
                .ocekivaniDatumTeljenja(ocekivaniDatum)
                .aktivna(true)
                .krava(krava)
                .veterinar(veterinar)
                .build();

        assertNotNull(steonost);
        assertEquals(3L, steonost.getId());
        assertEquals(datumPotvrde, steonost.getDatumPotvrde());
        assertEquals(ocekivaniDatum, steonost.getOcekivaniDatumTeljenja());
        assertTrue(steonost.isAktivna());
        assertEquals(krava, steonost.getKrava());
        assertEquals(veterinar, steonost.getVeterinar());
        assertNull(steonost.getTeljenje());
    }

    @Test
    @DisplayName("Datum potvrde ne sme biti null")
    void nullDatumPotvrdeVracaPovredu() {
        Steonost steonost = validnaSteonost();
        steonost.setDatumPotvrde(null);

        Set<ConstraintViolation<Steonost>> violations = validator.validate(steonost);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datumPotvrde"));
    }

    @Test
    @DisplayName("Datum potvrde ne sme biti u buducnosti")
    void buduciDatumPotvrdeVracaPovredu() {
        Steonost steonost = validnaSteonost();
        steonost.setDatumPotvrde(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<Steonost>> violations = validator.validate(steonost);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datumPotvrde"));
    }

    @Test
    @DisplayName("Ocekivani datum teljenja ne sme biti null")
    void nullOcekivaniDatumTeljenjaVracaPovredu() {
        Steonost steonost = validnaSteonost();
        steonost.setOcekivaniDatumTeljenja(null);

        Set<ConstraintViolation<Steonost>> violations = validator.validate(steonost);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "ocekivaniDatumTeljenja"));
    }

    @Test
    @DisplayName("Krava ne sme biti null")
    void nullKravaVracaPovredu() {
        Steonost steonost = validnaSteonost();
        steonost.setKrava(null);

        Set<ConstraintViolation<Steonost>> violations = validator.validate(steonost);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "krava"));
    }

    @Test
    @DisplayName("Veterinar ne sme biti null")
    void nullVeterinarVracaPovredu() {
        Steonost steonost = validnaSteonost();
        steonost.setVeterinar(null);

        Set<ConstraintViolation<Steonost>> violations = validator.validate(steonost);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "veterinar"));
    }

    private Steonost validnaSteonost() {
        return Steonost.builder()
                .id(3L)
                .datumPotvrde(LocalDate.now().minusDays(1))
                .ocekivaniDatumTeljenja(LocalDate.now().plusMonths(9))
                .aktivna(true)
                .krava(krava)
                .veterinar(veterinar)
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
