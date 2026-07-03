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

class TeljenjeTest {

    private Validator validator;
    private Krava krava;
    private Steonost steonost;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        krava = Krava.builder().id(1L).build();
        steonost = Steonost.builder().id(2L).krava(krava).build();
    }

    @Test
    @DisplayName("Ispravno teljenje prolazi validaciju")
    void validnoTeljenjeNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Teljenje>> violations = validator.validate(validnoTeljenje());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder pravilno postavlja podatke i relacije")
    void builderPostavljaPodatkeIRelacije() {
        LocalDate datum = LocalDate.of(2026, 6, 1);
        Teljenje teljenje = Teljenje.builder()
                .id(3L)
                .datum(datum)
                .brojTeladi(1)
                .napomena("Bez komplikacija")
                .krava(krava)
                .steonost(steonost)
                .build();

        assertNotNull(teljenje);
        assertEquals(3L, teljenje.getId());
        assertEquals(datum, teljenje.getDatum());
        assertEquals(1, teljenje.getBrojTeladi());
        assertEquals("Bez komplikacija", teljenje.getNapomena());
        assertEquals(krava, teljenje.getKrava());
        assertEquals(steonost, teljenje.getSteonost());
    }

    @Test
    @DisplayName("Datum teljenja ne sme biti null")
    void nullDatumVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setDatum(null);

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Datum teljenja ne sme biti u buducnosti")
    void buduciDatumVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setDatum(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Broj teladi mora biti najmanje jedan")
    void brojTeladiManjiOdJedanVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setBrojTeladi(0);

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "brojTeladi"));
    }

    @Test
    @DisplayName("Napomena ne sme imati vise od 500 karaktera")
    void predugackaNapomenaVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setNapomena("N".repeat(501));

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "napomena"));
    }

    @Test
    @DisplayName("Krava ne sme biti null")
    void nullKravaVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setKrava(null);

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "krava"));
    }

    @Test
    @DisplayName("Steonost ne sme biti null")
    void nullSteonostVracaPovredu() {
        Teljenje teljenje = validnoTeljenje();
        teljenje.setSteonost(null);

        Set<ConstraintViolation<Teljenje>> violations = validator.validate(teljenje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "steonost"));
    }

    private Teljenje validnoTeljenje() {
        return Teljenje.builder()
                .id(3L)
                .datum(LocalDate.now().minusDays(1))
                .brojTeladi(1)
                .napomena("Bez komplikacija")
                .krava(krava)
                .steonost(steonost)
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
