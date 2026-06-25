package rs.fon.bg.ac.rs.farma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProizvodnjaMlekaResponse(
        Long id,
        Long kravaId,
        String brojMarkice,
        LocalDate datum,
        BigDecimal jutarnjaLitara,
        BigDecimal vecernjaLitara,
        BigDecimal ukupnoLitara
) { }
