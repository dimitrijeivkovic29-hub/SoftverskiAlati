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
@Table(name = "teljenje")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teljenje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate datum;

    @Min(1)
    @Column(name = "broj_teladi", nullable = false)
    private int brojTeladi;

    @Size(max = 500)
    @Column(length = 500)
    private String napomena;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "krava_id", nullable = false)
    private Krava krava;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steonost_id", nullable = false, unique = true)
    private Steonost steonost;
}
