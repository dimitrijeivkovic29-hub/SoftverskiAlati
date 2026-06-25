package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.KravaRequest;
import rs.fon.bg.ac.rs.farma.dto.KravaResponse;
import rs.fon.bg.ac.rs.farma.service.KravaService;

import java.util.List;

@RestController
@RequestMapping("/api/krave")
public class KravaController {
    private final KravaService service;

    public KravaController(KravaService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<KravaResponse> dodaj(@Valid @RequestBody KravaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dodaj(request));
    }

    @PutMapping("/{id}")
    public KravaResponse izmeni(@PathVariable Long id, @Valid @RequestBody KravaRequest request) {
        return service.izmeni(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(@PathVariable Long id) {
        service.obrisi(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<KravaResponse> pretrazi(
            @RequestParam(required = false) String brojMarkice,
            @RequestParam(required = false) StatusKrave status) {
        return service.pretrazi(brojMarkice, status);
    }
}
