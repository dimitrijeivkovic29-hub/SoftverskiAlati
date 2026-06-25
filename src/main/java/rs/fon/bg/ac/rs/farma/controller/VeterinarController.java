package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.VeterinarRequest;
import rs.fon.bg.ac.rs.farma.dto.VeterinarResponse;
import rs.fon.bg.ac.rs.farma.service.VeterinarService;

import java.util.List;

@RestController
@RequestMapping("/api/veterinari")
public class VeterinarController {
    private final VeterinarService service;

    public VeterinarController(VeterinarService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<VeterinarResponse> dodaj(@Valid @RequestBody VeterinarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dodaj(request));
    }

    @GetMapping
    public List<VeterinarResponse> svi() { return service.svi(); }
}
