package rs.fon.bg.ac.rs.farma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bik", uniqueConstraints = @UniqueConstraint(name = "uk_bik_hb_broj", columnNames = "hb_broj"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bik {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String naziv;

    @NotBlank
    @Size(max = 40)
    @Column(name = "hb_broj", nullable = false, length = 40)
    private String hbBroj;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String rasa;

    @OneToMany(mappedBy = "bik")
    @Builder.Default
    private List<Osemenjavanje> osemenjavanja = new ArrayList<>();
}
