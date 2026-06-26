package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Predstavlja dnevnu proizvodnju mleka jedne krave.
 * Cuva jutarnju, vecernju i ukupnu kolicinu mleka. Ukupna kolicina se
 * automatski izracunava pre cuvanja i izmene entiteta.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "proizvodnja_mleka", uniqueConstraints = @UniqueConstraint(
        name = "uk_mleko_krava_datum", columnNames = {"krava_id", "datum"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProizvodnjaMleka {
    /**
     * Jedinstveni identifikator dnevnog unosa proizvodnje mleka.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Datum na koji se proizvodnja mleka odnosi.
     * Dozvoljene vrednosti: datum koji nije null i nije u buducnosti. Kombinacija krave
     * i datuma mora biti jedinstvena.
     */
    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    /**
     * Kolicina mleka iz jutarnje muze, izrazena u litrima.
     * Dozvoljene vrednosti: broj koji nije null i veci je ili jednak nuli;
     * u bazi se cuva sa najvise dve decimale.
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "jutarnja_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal jutarnjaLitara;

    /**
     * Kolicina mleka iz vecernje muze, izrazena u litrima.
     * Dozvoljene vrednosti: broj koji nije null i veci je ili jednak nuli;
     * u bazi se cuva sa najvise dve decimale.
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "vecernja_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal vecernjaLitara;

    /**
     * Ukupna dnevna kolicina mleka, izrazena u litrima.
     * Vrednost je obavezna, ne moze biti negativna i izracunava se kao zbir jutarnje
     * i vecernje muze, zaokruzen na dve decimale.
     */
    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "ukupno_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal ukupnoLitara;

    /**
     * Krava na koju se dnevni unos proizvodnje odnosi.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;
    /**
     * Racuna ukupnu dnevnu kolicinu mleka pre cuvanja ili izmene entiteta.
     * Null vrednosti jutarnje ili vecernje muze tretiraju se kao nula, a rezultat se
     * zaokruzuje na dve decimale primenom pravila HALF_UP.
     */
    @PrePersist
    @PreUpdate
    public void izracunajUkupno() {
        BigDecimal jutro = jutarnjaLitara == null ? BigDecimal.ZERO : jutarnjaLitara;
        BigDecimal vece = vecernjaLitara == null ? BigDecimal.ZERO : vecernjaLitara;
        ukupnoLitara = jutro.add(vece).setScale(2, RoundingMode.HALF_UP);
    }
}
