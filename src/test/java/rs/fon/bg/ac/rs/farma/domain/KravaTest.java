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

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KravaTest {

    private Validator validator;
    private Farma farma;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        farma = Farma.builder()
                .id(1L)
                .naziv("Zelena dolina")
                .adresa("Glavna 1")
                .pib("123456789")
                .build();
    }

    @Test
    @DisplayName("Ispravna krava prolazi validaciju")
    void validnaKravaNemaPovredeOgranicenja() {
        Set<ConstraintViolation<Krava>> violations = validator.validate(validnaKrava());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder i Lombok metode pravilno postavljaju podatke i relacije")
    void builderISeteriPostavljajuPodatkeIRelacije() {
        LocalDate datumRodjenja = LocalDate.of(2023, 3, 10);
        Krava krava = Krava.builder()
                .id(3L)
                .brojMarkice("RS-001")
                .datumRodjenja(datumRodjenja)
                .rasa("Holstajn")
                .laktacija(1)
                .status(StatusKrave.ZA_OSEMENJAVANJE)
                .farma(farma)
                .build();

        assertNotNull(krava);
        assertEquals(3L, krava.getId());
        assertEquals("RS-001", krava.getBrojMarkice());
        assertEquals(datumRodjenja, krava.getDatumRodjenja());
        assertEquals("Holstajn", krava.getRasa());
        assertEquals(1, krava.getLaktacija());
        assertEquals(StatusKrave.ZA_OSEMENJAVANJE, krava.getStatus());
        assertEquals(farma, krava.getFarma());
        assertNotNull(krava.getOsemenjavanja());
        assertNotNull(krava.getSteonosti());
        assertNotNull(krava.getTeljenja());
        assertNotNull(krava.getProizvodnjaMleka());
        assertTrue(krava.getOsemenjavanja().isEmpty());
        assertTrue(krava.getSteonosti().isEmpty());
        assertTrue(krava.getTeljenja().isEmpty());
        assertTrue(krava.getProizvodnjaMleka().isEmpty());

        krava.setStatus(StatusKrave.U_LAKTACIJI);
        krava.setLaktacija(2);

        assertEquals(StatusKrave.U_LAKTACIJI, krava.getStatus());
        assertEquals(2, krava.getLaktacija());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("Broj markice ne sme biti null, prazan ili blanko")
    void neispravanBrojMarkiceVracaPovredu(String brojMarkice) {
        Krava krava = validnaKrava();
        krava.setBrojMarkice(brojMarkice);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "brojMarkice"));
    }

    @Test
    @DisplayName("Broj markice ne sme imati vise od 30 karaktera")
    void predugacakBrojMarkiceVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setBrojMarkice("M".repeat(31));

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "brojMarkice"));
    }

    @Test
    @DisplayName("Datum rodjenja ne sme biti null")
    void nullDatumRodjenjaVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setDatumRodjenja(null);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datumRodjenja"));
    }

    @Test
    @DisplayName("Datum rodjenja ne sme biti u buducnosti")
    void buduciDatumRodjenjaVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setDatumRodjenja(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datumRodjenja"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\n"})
    @DisplayName("Rasa ne sme biti null, prazna ili blanko")
    void neispravnaRasaVracaPovredu(String rasa) {
        Krava krava = validnaKrava();
        krava.setRasa(rasa);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "rasa"));
    }

    @Test
    @DisplayName("Rasa ne sme imati vise od 80 karaktera")
    void predugackaRasaVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setRasa("R".repeat(81));

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "rasa"));
    }

    @Test
    @DisplayName("Laktacija ne sme biti negativna")
    void negativnaLaktacijaVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setLaktacija(-1);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "laktacija"));
    }

    @Test
    @DisplayName("Status krave ne sme biti null")
    void nullStatusVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setStatus(null);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "status"));
    }

    @Test
    @DisplayName("Farma krave ne sme biti null")
    void nullFarmaVracaPovredu() {
        Krava krava = validnaKrava();
        krava.setFarma(null);

        Set<ConstraintViolation<Krava>> violations = validator.validate(krava);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "farma"));
    }

    private Krava validnaKrava() {
        return Krava.builder()
                .id(3L)
                .brojMarkice("RS-001")
                .datumRodjenja(LocalDate.now().minusYears(3))
                .rasa("Holstajn")
                .laktacija(1)
                .status(StatusKrave.ZA_OSEMENJAVANJE)
                .farma(farma)
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
