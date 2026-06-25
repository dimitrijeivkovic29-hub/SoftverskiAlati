package rs.fon.bg.ac.rs.farma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record IzvestajMlekaResponse(
        Long kravaId,
        String brojMarkice,
        LocalDate od,
        LocalDate doDatuma,
        BigDecimal ukupnoLitara,
        BigDecimal prosekPoDanu,
        List<ProizvodnjaMlekaResponse> stavke
) { }
