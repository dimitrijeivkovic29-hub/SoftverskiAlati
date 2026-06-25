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

@Entity
@Table(name = "steonost")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Steonost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(name = "datum_potvrde", nullable = false)
    private LocalDate datumPotvrde;

    @NotNull
    @Column(name = "ocekivani_datum_teljenja", nullable = false)
    private LocalDate ocekivaniDatumTeljenja;

    @Column(nullable = false)
    private boolean aktivna;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinar_id", nullable = false)
    private Veterinar veterinar;

    @OneToOne(mappedBy = "steonost", fetch = FetchType.LAZY)
    private Teljenje teljenje;
}
