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

@Entity
@Table(name = "veterinar", uniqueConstraints = @UniqueConstraint(name = "uk_veterinar_telefon", columnNames = "telefon"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veterinar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String ime;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String prezime;

    @NotBlank
    @Pattern(regexp = "[+0-9 /-]{6,25}", message = "Telefon nije u ispravnom formatu")
    @Column(nullable = false, length = 25)
    private String telefon;

    @OneToMany(mappedBy = "veterinar")
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();

    @OneToMany(mappedBy = "veterinar")
    @Builder.Default
    private List<Steonost> potvrdeSteonosti = new ArrayList<>();
}
