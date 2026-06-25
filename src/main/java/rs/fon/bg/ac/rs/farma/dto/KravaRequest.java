package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.*;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;

import java.time.LocalDate;

public record KravaRequest(
        @NotBlank @Size(max = 30) String brojMarkice,
        @NotNull @PastOrPresent LocalDate datumRodjenja,
        @NotBlank @Size(max = 80) String rasa,
        @Min(0) int laktacija,
        @NotNull StatusKrave status,
        @NotNull Long farmaId
) { }
