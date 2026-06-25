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

@Entity
@Table(name = "krava", uniqueConstraints = @UniqueConstraint(name = "uk_krava_broj_markice", columnNames = "broj_markice"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Krava {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(name = "broj_markice", nullable = false, length = 30)
    private String brojMarkice;

    @NotNull
    @PastOrPresent
    @Column(name = "datum_rodjenja", nullable = false)
    private LocalDate datumRodjenja;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String rasa;

    @Min(0)
    @Column(nullable = false)
    private int laktacija;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusKrave status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farma_id", nullable = false)
    private Farma farma;

    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();

    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Steonost> steonosti = new ArrayList<>();

    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Teljenje> teljenja = new ArrayList<>();

    @OneToMany(mappedBy = "krava", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProizvodnjaMleka> proizvodnjaMleka = new ArrayList<>();
}
