package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BikRequest(
        @NotBlank @Size(max = 100) String naziv,
        @NotBlank @Size(max = 40) String hbBroj,
        @NotBlank @Size(max = 80) String rasa
) { }
