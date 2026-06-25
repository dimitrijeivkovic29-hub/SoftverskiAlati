package rs.fon.bg.ac.rs.farma.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProizvodnjaMlekaRequest(
        @NotNull Long kravaId,
        @NotNull @PastOrPresent LocalDate datum,
        @NotNull @DecimalMin("0.0") BigDecimal jutarnjaLitara,
        @NotNull @DecimalMin("0.0") BigDecimal vecernjaLitara
) { }
