package rs.fon.bg.ac.rs.farma.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        LocalDateTime vreme,
        int status,
        String greska,
        String poruka,
        String putanja,
        Map<String, String> detalji
) { }
