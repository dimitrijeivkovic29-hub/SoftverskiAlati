package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Krava;
import rs.fon.bg.ac.rs.farma.domain.ProizvodnjaMleka;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.IzvestajMlekaResponse;
import rs.fon.bg.ac.rs.farma.dto.ProizvodnjaMlekaRequest;
import rs.fon.bg.ac.rs.farma.dto.ProizvodnjaMlekaResponse;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.KravaRepository;
import rs.fon.bg.ac.rs.farma.repository.ProizvodnjaMlekaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProizvodnjaMlekaService {
    private final ProizvodnjaMlekaRepository repository;
    private final KravaRepository kravaRepository;

    public ProizvodnjaMlekaService(ProizvodnjaMlekaRepository repository, KravaRepository kravaRepository) {
        this.repository = repository;
        this.kravaRepository = kravaRepository;
    }

    @Transactional
    public ProizvodnjaMlekaResponse unesi(ProizvodnjaMlekaRequest request) {
        Krava krava = getKrava(request.kravaId());
        if (krava.getStatus() == StatusKrave.IZLUCENA) {
            throw new BusinessException("Nije moguce uneti mleko za izlucenu kravu");
        }
        if (repository.existsByKravaIdAndDatum(krava.getId(), request.datum())) {
            throw new DuplicateResourceException("Proizvodnja za izabrani datum vec postoji");
        }
        ProizvodnjaMleka p = new ProizvodnjaMleka();
        p.setKrava(krava);
        p.setDatum(request.datum());
        p.setJutarnjaLitara(request.jutarnjaLitara());
        p.setVecernjaLitara(request.vecernjaLitara());
        p.izracunajUkupno();
        return toResponse(repository.save(p));
    }

    @Transactional(readOnly = true)
    public IzvestajMlekaResponse pregled(Long kravaId, LocalDate od, LocalDate doDatuma) {
        if (od.isAfter(doDatuma)) {
            throw new BusinessException("Pocetni datum ne moze biti posle krajnjeg");
        }
        Krava krava = getKrava(kravaId);
        List<ProizvodnjaMlekaResponse> stavke = repository
                .findByKravaIdAndDatumBetweenOrderByDatumAsc(kravaId, od, doDatuma)
                .stream().map(this::toResponse).toList();
        BigDecimal ukupno = stavke.stream().map(ProizvodnjaMlekaResponse::ukupnoLitara)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prosek = stavke.isEmpty() ? BigDecimal.ZERO
                : ukupno.divide(BigDecimal.valueOf(stavke.size()), 2, RoundingMode.HALF_UP);
        return new IzvestajMlekaResponse(kravaId, krava.getBrojMarkice(), od, doDatuma,
                ukupno.setScale(2, RoundingMode.HALF_UP), prosek, stavke);
    }

    private Krava getKrava(Long id) {
        return kravaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Krava nije pronadjena: " + id));
    }

    private ProizvodnjaMlekaResponse toResponse(ProizvodnjaMleka p) {
        return new ProizvodnjaMlekaResponse(p.getId(), p.getKrava().getId(), p.getKrava().getBrojMarkice(),
                p.getDatum(), p.getJutarnjaLitara(), p.getVecernjaLitara(), p.getUkupnoLitara());
    }
}
