package rs.fon.bg.ac.rs.farma.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.UvozRezultatResponse;
import rs.fon.bg.ac.rs.farma.service.JsonService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/json")
public class JsonController {
    private final JsonService service;

    public JsonController(JsonService service) { this.service = service; }

    @GetMapping(value = "/izvoz", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> izvezi() {
        String filename = "farma-izvoz-" + LocalDate.now() + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.izvezi());
    }

    @PostMapping(value = "/uvoz", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UvozRezultatResponse> uvezi(@RequestBody byte[] json) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uvezi(json));
    }
}
