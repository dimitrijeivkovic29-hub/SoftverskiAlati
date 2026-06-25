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
@Table(name = "farma", uniqueConstraints = @UniqueConstraint(name = "uk_farma_pib", columnNames = "pib"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String naziv;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String adresa;

    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "PIB mora imati tacno 9 cifara")
    @Column(nullable = false, length = 9)
    private String pib;

    @OneToMany(mappedBy = "farma", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Krava> krave = new ArrayList<>();
}
