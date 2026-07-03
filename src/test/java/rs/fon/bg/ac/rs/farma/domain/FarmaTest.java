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

class FarmaTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Ispravna farma prolazi validaciju")
    void validnaFarmaNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Farma>> violations = validator.validate(validnaFarma());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder i Lombok metode pravilno postavljaju podatke")
    void builderISeteriPostavljajuPodatkeIInicijalizujuKrave() {
        Farma farma = Farma.builder()
                .id(1L)
                .naziv("Zelena dolina")
                .adresa("Glavna 1")
                .pib("123456789")
                .build();

        assertNotNull(farma);
        assertEquals(1L, farma.getId());
        assertEquals("Zelena dolina", farma.getNaziv());
        assertEquals("Glavna 1", farma.getAdresa());
        assertEquals("123456789", farma.getPib());
        assertNotNull(farma.getKrave());
        assertTrue(farma.getKrave().isEmpty());

        farma.setNaziv("Nova farma");
        farma.setAdresa("Nova 2");
        farma.setPib("987654321");

        assertEquals("Nova farma", farma.getNaziv());
        assertEquals("Nova 2", farma.getAdresa());
        assertEquals("987654321", farma.getPib());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Naziv farme ne sme biti null, prazan ili blanko")
    void neispravanNazivVracaPovredu(String naziv) {
        Farma farma = validnaFarma();
        farma.setNaziv(naziv);

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "naziv"));
    }

    @Test
    @DisplayName("Naziv farme ne sme imati vise od 120 karaktera")
    void predugacakNazivVracaPovredu() {
        Farma farma = validnaFarma();
        farma.setNaziv("N".repeat(121));

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "naziv"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Adresa ne sme biti null, prazna ili blanko")
    void neispravnaAdresaVracaPovredu(String adresa) {
        Farma farma = validnaFarma();
        farma.setAdresa(adresa);

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "adresa"));
    }

    @Test
    @DisplayName("Adresa ne sme imati vise od 200 karaktera")
    void predugackaAdresaVracaPovredu() {
        Farma farma = validnaFarma();
        farma.setAdresa("A".repeat(201));

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "adresa"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("PIB ne sme biti null, prazan ili blanko")
    void prazanPibVracaPovredu(String pib) {
        Farma farma = validnaFarma();
        farma.setPib(pib);

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "pib"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "1234567890", "12345ABCD", "123-45678"})
    @DisplayName("PIB mora imati tacno devet cifara")
    void pibNeispravnogFormataVracaPovredu(String pib) {
        Farma farma = validnaFarma();
        farma.setPib(pib);

        Set<ConstraintViolation<Farma>> violations = validator.validate(farma);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "pib"));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("PIB mora imati tacno 9 cifara")));
    }

    private Farma validnaFarma() {
        return Farma.builder()
                .id(1L)
                .naziv("Zelena dolina")
                .adresa("Glavna 1")
                .pib("123456789")
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
