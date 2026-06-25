package rs.fon.bg.ac.rs.farma.dto;

import java.time.LocalDate;

public record SteonostResponse(
        Long id,
        Long kravaId,
        String brojMarkice,
        LocalDate datumPotvrde,
        LocalDate ocekivaniDatumTeljenja,
        boolean aktivna,
        Long veterinarId,
        String veterinar
) { }
