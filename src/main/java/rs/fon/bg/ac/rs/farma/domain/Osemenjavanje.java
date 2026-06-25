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

@Entity
@Table(name = "osemenjavanje", uniqueConstraints = @UniqueConstraint(
        name = "uk_osemenjavanje_krava_redni_broj", columnNames = {"krava_id", "redni_broj"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Osemenjavanje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    @Min(1)
    @Column(name = "redni_broj", nullable = false)
    private int redniBroj;

    @Size(max = 500)
    @Column(length = 500)
    private String napomena;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bik_id", nullable = false)
    private Bik bik;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinar_id", nullable = false)
    private Veterinar veterinar;
}
