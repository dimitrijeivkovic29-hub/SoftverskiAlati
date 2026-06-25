package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.IzvestajMlekaResponse;
import rs.fon.bg.ac.rs.farma.dto.ProizvodnjaMlekaRequest;
import rs.fon.bg.ac.rs.farma.dto.ProizvodnjaMlekaResponse;
import rs.fon.bg.ac.rs.farma.service.ProizvodnjaMlekaService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mleko")
public class ProizvodnjaMlekaController {
    private final ProizvodnjaMlekaService service;

    public ProizvodnjaMlekaController(ProizvodnjaMlekaService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ProizvodnjaMlekaResponse> unesi(@Valid @RequestBody ProizvodnjaMlekaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.unesi(request));
    }

    @GetMapping("/krave/{kravaId}")
    public IzvestajMlekaResponse pregled(
            @PathVariable Long kravaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
            @RequestParam(name = "do") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate doDatuma) {
        return service.pregled(kravaId, od, doDatuma);
    }
}
