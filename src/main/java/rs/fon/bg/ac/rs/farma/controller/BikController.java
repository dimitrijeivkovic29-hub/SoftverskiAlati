package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.BikRequest;
import rs.fon.bg.ac.rs.farma.dto.BikResponse;
import rs.fon.bg.ac.rs.farma.service.BikService;

import java.util.List;

@RestController
@RequestMapping("/api/bikovi")
public class BikController {
    private final BikService service;

    public BikController(BikService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BikResponse> dodaj(@Valid @RequestBody BikRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dodaj(request));
    }

    @PutMapping("/{id}")
    public BikResponse izmeni(@PathVariable Long id, @Valid @RequestBody BikRequest request) {
        return service.izmeni(id, request);
    }

    @GetMapping
    public List<BikResponse> svi() { return service.svi(); }
}
