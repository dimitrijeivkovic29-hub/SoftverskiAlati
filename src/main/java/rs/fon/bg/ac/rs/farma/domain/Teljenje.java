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
 * Predstavlja evidentirano teljenje povezano sa aktivnom steonoscu.
 * Cuva datum teljenja, broj teladi, opcionu napomenu i veze ka kravi
 * i steonosti koja je teljenjem zavrsena.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "teljenje")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teljenje {
    /**
     * Jedinstveni identifikator teljenja.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Datum teljenja.
     * Dozvoljene vrednosti: datum koji nije null i nije u buducnosti.
     */
    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    /**
     * Broj oteljenih teladi.
     * Dozvoljene vrednosti: ceo broj veci ili jednak jedan.
     */
    @Min(1)
    @Column(name = "broj_teladi", nullable = false)
    private int brojTeladi;

    /**
     * Dodatna napomena o teljenju.
     * Polje je opciono, a maksimalna duzina je 500 karaktera.
     */
    @Size(max = 500)
    @Column(length = 500)
    private String napomena;

    /**
     * Krava koja se otelila.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    /**
     * Steonost koja je zavrsena ovim teljenjem.
     * Veza je obavezna, ucitava se lenjo i mora biti jedinstvena, pa jedna steonost
     * moze biti povezana sa najvise jednim teljenjem.
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steonost_id", nullable = false, unique = true)
    private Steonost steonost;
}
