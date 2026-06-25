package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TeljenjeRequest(
        @NotNull Long kravaId,
        @NotNull @PastOrPresent LocalDate datum,
        @Min(1) int brojTeladi,
        @Size(max = 500) String napomena
) { }
