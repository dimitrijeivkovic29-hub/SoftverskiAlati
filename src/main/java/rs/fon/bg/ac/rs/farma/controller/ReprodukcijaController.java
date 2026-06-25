package rs.fon.bg.ac.rs.farma.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.*;
import rs.fon.bg.ac.rs.farma.service.ReprodukcijaService;

import java.util.List;

@RestController
@RequestMapping("/api/reprodukcija")
public class ReprodukcijaController {
    private final ReprodukcijaService service;

    public ReprodukcijaController(ReprodukcijaService service) { this.service = service; }

    @PostMapping("/osemenjavanja")
    public ResponseEntity<OsemenjavanjeResponse> evidentirajOsemenjavanje(
            @Valid @RequestBody OsemenjavanjeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.evidentirajOsemenjavanje(request));
    }

    @GetMapping("/krave/{kravaId}/osemenjavanja")
    public List<OsemenjavanjeResponse> istorija(@PathVariable Long kravaId) {
        return service.istorijaOsemenjavanja(kravaId);
    }

    @GetMapping("/provera-steonosti")
    public List<ProveraSteonostiResponse> zaProveru(
            @RequestParam(defaultValue = "28") int minimalniBrojDana) {
        return service.kraveZaProveru(minimalniBrojDana);
    }

    @PostMapping("/steonosti")
    public ResponseEntity<SteonostResponse> potvrdi(@Valid @RequestBody PotvrdaSteonostiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.potvrdiSteonost(request));
    }

    @PostMapping("/teljenja")
    public ResponseEntity<TeljenjeResponse> evidentirajTeljenje(@Valid @RequestBody TeljenjeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.evidentirajTeljenje(request));
    }
}
