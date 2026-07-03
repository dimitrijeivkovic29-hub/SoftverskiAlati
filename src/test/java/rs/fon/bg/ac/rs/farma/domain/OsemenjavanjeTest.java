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

class OsemenjavanjeTest {

    private Validator validator;
    private Krava krava;
    private Bik bik;
    private Veterinar veterinar;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        krava = Krava.builder().id(1L).build();
        bik = Bik.builder().id(2L).build();
        veterinar = Veterinar.builder().id(3L).build();
    }

    @Test
    @DisplayName("Ispravno osemenjavanje prolazi validaciju")
    void validnoOsemenjavanjeNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(validnoOsemenjavanje());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder pravilno postavlja podatke i povezuje ucesnike")
    void builderPostavljaPodatkeIPovezujeSveUcesnike() {
        LocalDate datum = LocalDate.of(2026, 5, 1);
        Osemenjavanje osemenjavanje = Osemenjavanje.builder()
                .id(4L)
                .datum(datum)
                .redniBroj(1)
                .napomena("Prvi pokusaj")
                .krava(krava)
                .bik(bik)
                .veterinar(veterinar)
                .build();

        assertNotNull(osemenjavanje);
        assertEquals(4L, osemenjavanje.getId());
        assertEquals(datum, osemenjavanje.getDatum());
        assertEquals(1, osemenjavanje.getRedniBroj());
        assertEquals("Prvi pokusaj", osemenjavanje.getNapomena());
        assertEquals(krava, osemenjavanje.getKrava());
        assertEquals(bik, osemenjavanje.getBik());
        assertEquals(veterinar, osemenjavanje.getVeterinar());
    }

    @Test
    @DisplayName("Datum osemenjavanja ne sme biti null")
    void nullDatumVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setDatum(null);

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Datum osemenjavanja ne sme biti u buducnosti")
    void buduciDatumVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setDatum(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Redni broj osemenjavanja mora biti najmanje jedan")
    void redniBrojManjiOdJedanVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setRedniBroj(0);

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "redniBroj"));
    }

    @Test
    @DisplayName("Napomena ne sme imati vise od 500 karaktera")
    void predugackaNapomenaVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setNapomena("N".repeat(501));

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "napomena"));
    }

    @Test
    @DisplayName("Krava ne sme biti null")
    void nullKravaVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setKrava(null);

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "krava"));
    }

    @Test
    @DisplayName("Bik ne sme biti null")
    void nullBikVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setBik(null);

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "bik"));
    }

    @Test
    @DisplayName("Veterinar ne sme biti null")
    void nullVeterinarVracaPovredu() {
        Osemenjavanje osemenjavanje = validnoOsemenjavanje();
        osemenjavanje.setVeterinar(null);

        Set<ConstraintViolation<Osemenjavanje>> violations = validator.validate(osemenjavanje);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "veterinar"));
    }

    private Osemenjavanje validnoOsemenjavanje() {
        return Osemenjavanje.builder()
                .id(4L)
                .datum(LocalDate.now().minusDays(1))
                .redniBroj(1)
                .napomena("Prvi pokusaj")
                .krava(krava)
                .bik(bik)
                .veterinar(veterinar)
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
