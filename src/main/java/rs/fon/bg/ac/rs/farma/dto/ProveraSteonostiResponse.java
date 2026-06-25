package rs.fon.bg.ac.rs.farma.dto;

import java.time.LocalDate;

public record ProveraSteonostiResponse(
        Long kravaId,
        String brojMarkice,
        LocalDate poslednjeOsemenjavanje,
        long danaOdOsemenjavanja
) { }
