package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FarmaRequest(
        @NotBlank @Size(max = 120) String naziv,
        @NotBlank @Size(max = 200) String adresa,
        @NotBlank @Pattern(regexp = "\\d{9}") String pib
) { }
