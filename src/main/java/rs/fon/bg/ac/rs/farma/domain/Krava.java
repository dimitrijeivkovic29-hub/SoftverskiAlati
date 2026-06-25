package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Predstavlja kravu evidentiranu na farmi.
 * Sadrzi identifikacione i proizvodne podatke, trenutni status, pripadnost farmi
 * i kompletnu istoriju reprodukcije i proizvodnje mleka.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "krava", uniqueConstraints = @UniqueConstraint(name = "uk_krava_broj_markice", columnNames = "broj_markice"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Krava {
    /**
     * Jedinstveni identifikator krave.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Jedinstveni broj usne markice krave.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 30 karaktera. Vrednost mora biti jedinstvena u sistemu.
     */
    @NotBlank
    @Size(max = 30)
    @Column(name = "broj_markice", nullable = false, length = 30)
    private String brojMarkice;

    /**
     * Datum rodjenja krave.
     * Dozvoljene vrednosti: datum koji nije null i nije u buducnosti.
     */
    @NotNull
    @PastOrPresent
    @Column(name = "datum_rodjenja", nullable = false)
    private LocalDate datumRodjenja;

    /**
     * Rasa krave.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 80 karaktera.
     */
    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String rasa;

    /**
     * Redni broj trenutne laktacije.
     * Dozvoljene vrednosti: ceo broj veci ili jednak nuli.
     */
    @Min(0)
    @Column(nullable = false)
    private int laktacija;

    /**
     * Trenutni status krave u proizvodnom i reproduktivnom ciklusu.
     * Vrednost je obavezna i cuva se kao tekstualna vrednost enumeracije StatusKrave.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusKrave status;

    /**
     * Farma kojoj krava pripada.
     * Veza je obavezna i ucitava se lenjo.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farma_id", nullable = false)
    private Farma farma;

    /**
     * Istorija osemenjavanja krave.
     * Kolekcija se inicijalizuje kao prazna lista. Cuvanje i brisanje se kaskadno prenose
     * na stavke, a uklonjene stavke se brisu iz baze.
     */
    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();

    /**
     * Istorija potvrdjenih steonosti krave.
     * Kolekcija se inicijalizuje kao prazna lista. Cuvanje i brisanje se kaskadno prenose
     * na stavke, a uklonjene stavke se brisu iz baze.
     */
    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Steonost> steonosti = new ArrayList<>();

    /**
     * Istorija teljenja krave.
     * Kolekcija se inicijalizuje kao prazna lista. Cuvanje i brisanje se kaskadno prenose
     * na stavke, a uklonjene stavke se brisu iz baze.
     */
    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Teljenje> teljenja = new ArrayList<>();

    /**
     * Dnevni unosi proizvodnje mleka za kravu.
     * Kolekcija se inicijalizuje kao prazna lista. Cuvanje i brisanje se kaskadno prenose
     * na stavke, a uklonjene stavke se brisu iz baze.
     */
    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProizvodnjaMleka> proizvodnjaMleka = new ArrayList<>();
}
