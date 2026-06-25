package rs.fon.bg.ac.rs.farma.dto;

import java.time.LocalDate;

public record TeljenjeResponse(
        Long id,
        Long kravaId,
        String brojMarkice,
        Long steonostId,
        LocalDate datum,
        int brojTeladi,
        String napomena
) { }
