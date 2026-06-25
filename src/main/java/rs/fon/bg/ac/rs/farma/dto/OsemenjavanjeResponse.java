package rs.fon.bg.ac.rs.farma.dto;

import java.time.LocalDate;

public record OsemenjavanjeResponse(
        Long id,
        LocalDate datum,
        int redniBroj,
        String napomena,
        Long kravaId,
        String brojMarkice,
        Long bikId,
        String bik,
        Long veterinarId,
        String veterinar
) { }
