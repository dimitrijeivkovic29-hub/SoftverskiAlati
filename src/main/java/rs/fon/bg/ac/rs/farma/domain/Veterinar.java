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
 * Predstavlja veterinara koji ucestvuje u reproduktivnim postupcima na farmi.
 * Cuva licne i kontakt podatke, kao i veze ka osemenjavanjima i potvrdama
 * steonosti koje je veterinar izvrsio.
 * @author Dimitrije Ivkovic
 */
@Entity
@Table(name = "veterinar", uniqueConstraints = @UniqueConstraint(name = "uk_veterinar_telefon", columnNames = "telefon"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veterinar {
    /**
     * Jedinstveni identifikator veterinara.
     * Vrednost automatski generise baza podataka prilikom cuvanja entiteta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ime veterinara.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 60 karaktera.
     */
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String ime;

    /**
     * Prezime veterinara.
     * Dozvoljene vrednosti: tekst koji nije null, prazan niti sastavljen samo od razmaka;
     * maksimalna duzina je 60 karaktera.
     */
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String prezime;

    /**
     * Jedinstveni kontakt telefon veterinara.
     * Dozvoljene vrednosti: od 6 do 25 znakova iz skupa cifara, znaka plus, razmaka,
     * kose crte i crtice. Vrednost ne sme biti prazna i mora biti jedinstvena.
     */
    @NotBlank
    @Pattern(regexp = "[+0-9 /-]{6,25}", message = "Telefon nije u ispravnom formatu")
    @Column(nullable = false, length = 25)
    private String telefon;

    /**
     * Osemenjavanja povezana sa veterinarom.
     * Kolekcija se inicijalizuje kao prazna lista i predstavlja inverznu stranu veze
     * sa entitetom Osemenjavanje.
     */
    @OneToMany(mappedBy = "veterinar")
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();

    /**
     * Potvrde steonosti koje je veterinar izvrsio.
     * Kolekcija se inicijalizuje kao prazna lista i predstavlja inverznu stranu veze
     * sa entitetom Steonost.
     */
    @OneToMany(mappedBy = "veterinar")
    @Builder.Default
    private List<Steonost> potvrdeSteonosti = new ArrayList<>();
}
