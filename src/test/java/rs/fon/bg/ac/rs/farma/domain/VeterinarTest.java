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

class VeterinarTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Ispravan veterinar prolazi validaciju")
    void validanVeterinarNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Veterinar>> violations = validator.validate(validanVeterinar());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder i Lombok metode pravilno postavljaju podatke")
    void builderISeteriPostavljajuPodatkeIInicijalizujuKolekcije() {
        Veterinar veterinar = Veterinar.builder()
                .id(5L)
                .ime("Ana")
                .prezime("Ilic")
                .telefon("060123456")
                .build();

        assertNotNull(veterinar);
        assertEquals(5L, veterinar.getId());
        assertEquals("Ana", veterinar.getIme());
        assertEquals("Ilic", veterinar.getPrezime());
        assertEquals("060123456", veterinar.getTelefon());
        assertNotNull(veterinar.getOsemenjavanja());
        assertNotNull(veterinar.getPotvrdeSteonosti());
        assertTrue(veterinar.getOsemenjavanja().isEmpty());
        assertTrue(veterinar.getPotvrdeSteonosti().isEmpty());

        veterinar.setIme("Marko");
        veterinar.setPrezime("Markovic");
        veterinar.setTelefon("+381 60 123-456");

        assertEquals("Marko", veterinar.getIme());
        assertEquals("Markovic", veterinar.getPrezime());
        assertEquals("+381 60 123-456", veterinar.getTelefon());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Ime ne sme biti null, prazno ili blanko")
    void neispravnoImeVracaPovredu(String ime) {
        Veterinar veterinar = validanVeterinar();
        veterinar.setIme(ime);

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "ime"));
    }

    @Test
    @DisplayName("Ime ne sme imati vise od 60 karaktera")
    void predugackoImeVracaPovredu() {
        Veterinar veterinar = validanVeterinar();
        veterinar.setIme("I".repeat(61));

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "ime"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Prezime ne sme biti null, prazno ili blanko")
    void neispravnoPrezimeVracaPovredu(String prezime) {
        Veterinar veterinar = validanVeterinar();
        veterinar.setPrezime(prezime);

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "prezime"));
    }

    @Test
    @DisplayName("Prezime ne sme imati vise od 60 karaktera")
    void predugackoPrezimeVracaPovredu() {
        Veterinar veterinar = validanVeterinar();
        veterinar.setPrezime("P".repeat(61));

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "prezime"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Telefon ne sme biti null, prazan ili blanko")
    void prazanTelefonVracaPovredu(String telefon) {
        Veterinar veterinar = validanVeterinar();
        veterinar.setTelefon(telefon);

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "telefon"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "060ABC123", "060_123456", "12345678901234567890123456"})
    @DisplayName("Telefon mora odgovarati propisanom formatu")
    void telefonNeispravnogFormataVracaPovredu(String telefon) {
        Veterinar veterinar = validanVeterinar();
        veterinar.setTelefon(telefon);

        Set<ConstraintViolation<Veterinar>> violations = validator.validate(veterinar);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "telefon"));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Telefon nije u ispravnom formatu")));
    }

    private Veterinar validanVeterinar() {
        return Veterinar.builder()
                .id(5L)
                .ime("Ana")
                .prezime("Ilic")
                .telefon("060123456")
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
