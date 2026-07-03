package rs.fon.bg.ac.rs.farma.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProizvodnjaMlekaTest {

    private Validator validator;
    private Krava krava;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        krava = Krava.builder().id(1L).build();
    }

    @Test
    @DisplayName("Ispravna proizvodnja mleka prolazi validaciju")
    void validnaProizvodnjaNemaPovredeOgranicenja() {
        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(validnaProizvodnja());

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Builder pravilno postavlja sve podatke")
    void builderPostavljaSvePodatke() {
        LocalDate datum = LocalDate.now().minusDays(1);
        ProizvodnjaMleka proizvodnja = ProizvodnjaMleka.builder()
                .id(5L)
                .datum(datum)
                .jutarnjaLitara(new BigDecimal("12.50"))
                .vecernjaLitara(new BigDecimal("10.25"))
                .ukupnoLitara(new BigDecimal("22.75"))
                .krava(krava)
                .build();

        assertNotNull(proizvodnja);
        assertEquals(5L, proizvodnja.getId());
        assertEquals(datum, proizvodnja.getDatum());
        assertEquals(new BigDecimal("12.50"), proizvodnja.getJutarnjaLitara());
        assertEquals(new BigDecimal("10.25"), proizvodnja.getVecernjaLitara());
        assertEquals(new BigDecimal("22.75"), proizvodnja.getUkupnoLitara());
        assertEquals(krava, proizvodnja.getKrava());
    }

    @Test
    @DisplayName("Ukupna kolicina je zbir jutarnje i vecernje muze zaokruzen na dve decimale")
    void izracunajUkupnoSabiraJutarnjuIVecernjuMuzu() {
        ProizvodnjaMleka proizvodnja = new ProizvodnjaMleka();
        proizvodnja.setJutarnjaLitara(new BigDecimal("12.345"));
        proizvodnja.setVecernjaLitara(new BigDecimal("10.251"));

        proizvodnja.izracunajUkupno();

        assertEquals(new BigDecimal("22.60"), proizvodnja.getUkupnoLitara());
    }

    @Test
    @DisplayName("Null vrednost muze se prilikom racunanja tretira kao nula")
    void izracunajUkupnoTretiraNullKaoNulu() {
        ProizvodnjaMleka proizvodnja = new ProizvodnjaMleka();
        proizvodnja.setVecernjaLitara(new BigDecimal("7.50"));

        proizvodnja.izracunajUkupno();

        assertEquals(new BigDecimal("7.50"), proizvodnja.getUkupnoLitara());
    }

    @Test
    @DisplayName("Datum proizvodnje ne sme biti null")
    void nullDatumVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setDatum(null);

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Datum proizvodnje ne sme biti u buducnosti")
    void buduciDatumVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setDatum(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "datum"));
    }

    @Test
    @DisplayName("Jutarnja kolicina mleka ne sme biti null")
    void nullJutarnjaLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setJutarnjaLitara(null);

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "jutarnjaLitara"));
    }

    @Test
    @DisplayName("Jutarnja kolicina mleka ne sme biti negativna")
    void negativnaJutarnjaLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setJutarnjaLitara(new BigDecimal("-0.01"));

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "jutarnjaLitara"));
    }

    @Test
    @DisplayName("Vecernja kolicina mleka ne sme biti null")
    void nullVecernjaLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setVecernjaLitara(null);

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "vecernjaLitara"));
    }

    @Test
    @DisplayName("Vecernja kolicina mleka ne sme biti negativna")
    void negativnaVecernjaLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setVecernjaLitara(new BigDecimal("-0.01"));

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "vecernjaLitara"));
    }

    @Test
    @DisplayName("Ukupna kolicina mleka ne sme biti null")
    void nullUkupnoLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setUkupnoLitara(null);

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "ukupnoLitara"));
    }

    @Test
    @DisplayName("Ukupna kolicina mleka ne sme biti negativna")
    void negativnoUkupnoLitaraVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setUkupnoLitara(new BigDecimal("-0.01"));

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "ukupnoLitara"));
    }

    @Test
    @DisplayName("Krava ne sme biti null")
    void nullKravaVracaPovredu() {
        ProizvodnjaMleka proizvodnja = validnaProizvodnja();
        proizvodnja.setKrava(null);

        Set<ConstraintViolation<ProizvodnjaMleka>> violations = validator.validate(proizvodnja);

        assertFalse(violations.isEmpty());
        assertTrue(imaPovreduZa(violations, "krava"));
    }

    private ProizvodnjaMleka validnaProizvodnja() {
        return ProizvodnjaMleka.builder()
                .id(5L)
                .datum(LocalDate.now().minusDays(1))
                .jutarnjaLitara(new BigDecimal("12.50"))
                .vecernjaLitara(new BigDecimal("10.25"))
                .ukupnoLitara(new BigDecimal("22.75"))
                .krava(krava)
                .build();
    }

    private boolean imaPovreduZa(Set<? extends ConstraintViolation<?>> violations, String polje) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(polje));
    }
}
