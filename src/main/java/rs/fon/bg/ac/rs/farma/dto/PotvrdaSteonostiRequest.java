package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PotvrdaSteonostiRequest(
        @NotNull Long kravaId,
        @NotNull Long veterinarId,
        @NotNull @PastOrPresent LocalDate datumPotvrde
) { }
