package rs.fon.bg.ac.rs.farma.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import rs.fon.bg.ac.rs.farma.dto.StatistikaFarmeResponse;
import rs.fon.bg.ac.rs.farma.service.StatistikaService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistika")
public class StatistikaController {
    private final StatistikaService service;

    public StatistikaController(StatistikaService service) { this.service = service; }

    @GetMapping("/farme/{farmaId}")
    public StatistikaFarmeResponse prikazi(
            @PathVariable Long farmaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
            @RequestParam(name = "do", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate doDatuma) {
        LocalDate kraj = doDatuma == null ? LocalDate.now() : doDatuma;
        LocalDate pocetak = od == null ? kraj.withDayOfMonth(1) : od;
        return service.prikazi(farmaId, pocetak, kraj);
    }
}
