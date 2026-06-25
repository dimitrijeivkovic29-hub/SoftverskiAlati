package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Predstavlja bika koji se koristi u reproduktivnim postupcima na farmi.
 * Entitet cuva osnovne podatke o biku, jedinstveni HB broj i vezu ka svim
 * osemenjavanjima u kojima je njegov reproduktivni materijal koriscen.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "bik", uniqueConstraints = @UniqueConstraint(name = "uk_bik_hb_broj", columnNames = "hb_broj"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bik {
    /**
     * Jedinstveni identifikator bika.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Naziv ili ime bika.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 100 karaktera.
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String naziv;

    /**
     * Jedinstveni HB broj bika.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 40 karaktera. Vrednost mora biti jedinstvena u sistemu.
     */
    @NotBlank
    @Size(max = 40)
    @Column(name = "hb_broj", nullable = false, length = 40)
    private String hbBroj;

    /**
     * Rasa bika.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 80 karaktera.
     */
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String rasa;

    /**
     * Osemenjavanja u kojima je bik koriscen
     * Kolekcija se inicijalizuje kao prazna lista i predstavlja inverznu stranu veze sa entitetom Osemenjavanje.
     */
    @OneToMany(mappedBy = "bik")
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();
}
