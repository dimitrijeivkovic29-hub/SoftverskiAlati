package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Predstavlja farmu mlecnih krava evidentiranu u sistemu.
 * Farma je korenski entitet koji sadrzi osnovne poslovne podatke i kolekciju
 * krava koje joj pripadaju.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "farma", uniqueConstraints = @UniqueConstraint(name = "uk_farma_pib", columnNames = "pib"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farma {
    /**
     * Jedinstveni identifikator farme.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Naziv farme.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 120 karaktera.
     */
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String naziv;

    /**
     * Adresa farme.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 200 karaktera.
     */
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String adresa;

    /**
     * Poreski identifikacioni broj farme.
     * Dozvoljene vrednosti: tacno devet cifara. Vrednost ne sme biti null ili prazna
     * i mora biti jedinstvena u sistemu.
     */
    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "PIB mora imati tacno 9 cifara")
    @Column(nullable = false, length = 9)
    private String pib;

    /**
     * Krave koje pripadaju farmi.
     * Kolekcija se inicijalizuje kao prazna lista. Operacije cuvanja i brisanja kaskadno se
     * prenose na krave, a uklanjanje krave iz kolekcije dovodi do njenog brisanja.
     */
    @OneToMany(mappedBy = "farma", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Krava> krave = new ArrayList<>();
}
