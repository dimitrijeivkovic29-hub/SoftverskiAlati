package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.FarmaRequest;
import rs.fon.bg.ac.rs.farma.dto.FarmaResponse;
import rs.fon.bg.ac.rs.farma.service.FarmaService;

import java.util.List;

@RestController
@RequestMapping("/api/farme")
public class FarmaController {
    private final FarmaService service;

    public FarmaController(FarmaService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<FarmaResponse> dodaj(@Valid @RequestBody FarmaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dodaj(request));
    }

    @GetMapping
    public List<FarmaResponse> sve() { return service.sve(); }

    @GetMapping("/{id}")
    public FarmaResponse nadji(@PathVariable Long id) { return service.nadji(id); }

    @PutMapping("/{id}")
    public FarmaResponse izmeni(@PathVariable Long id, @Valid @RequestBody FarmaRequest request) {
        return service.izmeni(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(@PathVariable Long id) {
        service.obrisi(id);
        return ResponseEntity.noContent().build();
    }
}
