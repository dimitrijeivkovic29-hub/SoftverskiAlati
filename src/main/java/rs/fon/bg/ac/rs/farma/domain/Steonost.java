package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Predstavlja potvrdjenu steonost krave.
 * Cuva datum potvrde, ocekivani datum teljenja, informaciju o aktivnosti
 * i veterinara koji je izvrsio potvrdu.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "steonost")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Steonost {
    /**
     * Jedinstveni identifikator steonosti.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Datum kada je steonost potvrdjena.
     * Dozvoljene vrednosti: datum koji nije null i nije u buducnosti.
     */
    @NotNull
    @PastOrPresent
    @Column(name = "datum_potvrde", nullable = false)
    private LocalDate datumPotvrde;

    /**
     * Ocekivani datum teljenja.
     * Vrednost je obavezna i izracunava se na osnovu datuma poslednjeg osemenjavanja.
     */
    @NotNull
    @Column(name = "ocekivani_datum_teljenja", nullable = false)
    private LocalDate ocekivaniDatumTeljenja;

    /**
     * Oznacava da li je steonost jos uvek aktivna.
     * Dozvoljene vrednosti: true za aktivnu steonost i false za zavrsenu steonost.
     */
    @Column(nullable = false)
    private boolean aktivna;

    /**
     * Krava na koju se steonost odnosi.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    /**
     * Veterinar koji je potvrdio steonost.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinar_id", nullable = false)
    private Veterinar veterinar;

    /**
     * Teljenje kojim je steonost zavrsena, ako postoji.
     * Veza je opciona, ucitava se lenjo i predstavlja inverznu stranu veze sa entitetom Teljenje.
     */
    @OneToOne(mappedBy = "steonost", fetch = FetchType.LAZY)
    private Teljenje teljenje;
}
