package rs.fon.bg.ac.rs.farma.dto;

import rs.fon.bg.ac.rs.farma.domain.StatusKrave;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record StatistikaFarmeResponse(
        Long farmaId,
        String nazivFarme,
        long brojKrava,
        Map<StatusKrave, Long> brojKravaPoStatusu,
        long brojBikova,
        long brojVeterinara,
        long brojAktivnihSteonosti,
        LocalDate periodOd,
        LocalDate periodDo,
        BigDecimal ukupnoMleka
) { }
