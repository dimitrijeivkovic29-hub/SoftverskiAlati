package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VeterinarRequest(
        @NotBlank @Size(max = 60) String ime,
        @NotBlank @Size(max = 60) String prezime,
        @NotBlank @Pattern(regexp = "[+0-9 /-]{6,25}") String telefon
) { }
