package rs.fon.bg.ac.rs.farma.dto;

public record UvozRezultatResponse(
        int farme,
        int krave,
        int bikovi,
        int veterinari,
        int osemenjavanja,
        int steonosti,
        int teljenja,
        int proizvodnjaMleka
) { }
