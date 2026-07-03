package rs.fon.bg.ac.rs.farma.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BikTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Ispravan bik prolazi validaciju")
    void validanBikNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Bik>> violations = validator.validate(validanBik());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder i Lombok metode pravilno postavljaju podatke")
    void builderISeteriPostavljajuPodatkeIInicijalizujuOsemenjavanja() {
        Bik bik = Bik.builder()
                .id(2L)
                .naziv("Atlas")
                .hbBroj("HB-22")
                .rasa("Holstajn")
                .build();

        assertNotNull(bik);
        assertEquals(2L, bik.getId());
        assertEquals("Atlas", bik.getNaziv());
        assertEquals("HB-22", bik.getHbBroj());
        assertEquals("Holstajn", bik.getRasa());
        assertNotNull(bik.getOsemenjavanja());
        assertTrue(bik.getOsemenjavanja().isEmpty());

        bik.setNaziv("Orion");
        bik.setHbBroj("HB-33");
        bik.setRasa("Simentalac");

        assertEquals("Orion", bik.getNaziv());
        assertEquals("HB-33", bik.getHbBroj());
        assertEquals("Simentalac", bik.getRasa());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Naziv ne sme biti null, prazan ili blanko")
    void neispravanNazivVracaPovredu(String naziv) {
        Bik bik = validanBik();
        bik.setNaziv(naziv);

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "naziv"));
    }

    @Test
    @DisplayName("Naziv ne sme imati vise od 100 karaktera")
    void predugacakNazivVracaPovredu() {
        Bik bik = validanBik();
        bik.setNaziv("A".repeat(101));

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "naziv"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("HB broj ne sme biti null, prazan ili blanko")
    void neispravanHbBrojVracaPovredu(String hbBroj) {
        Bik bik = validanBik();
        bik.setHbBroj(hbBroj);

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "hbBroj"));
    }

    @Test
    @DisplayName("HB broj ne sme imati vise od 40 karaktera")
    void predugacakHbBrojVracaPovredu() {
        Bik bik = validanBik();
        bik.setHbBroj("H".repeat(41));

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "hbBroj"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\n"})
    @DisplayName("Rasa ne sme biti null, prazna ili blanko")
    void neispravnaRasaVracaPovredu(String rasa) {
        Bik bik = validanBik();
        bik.setRasa(rasa);

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "rasa"));
    }

    @Test
    @DisplayName("Rasa ne sme imati vise od 80 karaktera")
    void predugackaRasaVracaPovredu() {
        Bik bik = validanBik();
        bik.setRasa("R".repeat(81));

        Set<ConstraintViolation<Bik>> violations = validator.validate(bik);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "rasa"));
    }

    private Bik validanBik() {
        return Bik.builder()
                .id(1L)
                .naziv("Atlas")
                .hbBroj("HB-22")
                .rasa("Holstajn")
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
