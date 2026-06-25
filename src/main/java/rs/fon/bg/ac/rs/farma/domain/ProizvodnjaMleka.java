package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "proizvodnja_mleka", uniqueConstraints = @UniqueConstraint(
        name = "uk_mleko_krava_datum", columnNames = {"krava_id", "datum"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProizvodnjaMleka {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "jutarnja_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal jutarnjaLitara;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "vecernja_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal vecernjaLitara;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "ukupno_litara", nullable = false, precision = 10, scale = 2)
    private BigDecimal ukupnoLitara;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;
    @PrePersist
    @PreUpdate
    public void izracunajUkupno() {
        BigDecimal jutro = jutarnjaLitara == null ? BigDecimal.ZERO : jutarnjaLitara;
        BigDecimal vece = vecernjaLitara == null ? BigDecimal.ZERO : vecernjaLitara;
        ukupnoLitara = jutro.add(vece).setScale(2, RoundingMode.HALF_UP);
    }
}
