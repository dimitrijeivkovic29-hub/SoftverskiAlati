package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Predstavlja jedno evidentirano osemenjavanje krave.
 * Povezuje kravu, bika i veterinara i cuva datum postupka, redni broj pokusaja
 * i opcionu napomenu.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "osemenjavanje", uniqueConstraints = @UniqueConstraint(
        name = "uk_osemenjavanje_krava_redni_broj", columnNames = {"krava_id", "redni_broj"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Osemenjavanje {
    /**
     * Jedinstveni identifikator osemenjavanja.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Datum kada je osemenjavanje obavljeno.
     * Dozvoljene vrednosti: datum koji nije null i nije u buducnosti.
     */
    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    /**
     * Redni broj pokusaja osemenjavanja za izabranu kravu.
     * Dozvoljene vrednosti: ceo broj veci ili jednak jedan. Kombinacija krave i rednog broja
     * mora biti jedinstvena.
     */
    @Min(1)
    @Column(name = "redni_broj", nullable = false)
    private int redniBroj;

    /**
     * Dodatna napomena o postupku.
     * Polje je opciono, a maksimalna duzina je 500 karaktera.
     */
    @Size(max = 500)
    @Column(length = 500)
    private String napomena;

    /**
     * Krava koja je osemenjena.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    /**
     * Bik ciji je reproduktivni materijal koriscen.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bik_id", nullable = false)
    private Bik bik;

    /**
     * Veterinar koji je evidentirao ili izvrsio postupak.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinar_id", nullable = false)
    private Veterinar veterinar;
}
