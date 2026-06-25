package rs.fon.bg.ac.rs.farma.dto;

import rs.fon.bg.ac.rs.farma.domain.StatusKrave;

import java.time.LocalDate;

public record KravaResponse(
        Long id,
        String brojMarkice,
        LocalDate datumRodjenja,
        String rasa,
        int laktacija,
        StatusKrave status,
        Long farmaId,
        String nazivFarme
) { }
