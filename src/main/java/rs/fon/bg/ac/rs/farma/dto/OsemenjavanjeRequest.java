package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OsemenjavanjeRequest(
        @NotNull Long kravaId,
        @NotNull Long bikId,
        @NotNull Long veterinarId,
        @NotNull @PastOrPresent LocalDate datum,
        @Size(max = 500) String napomena
) { }
