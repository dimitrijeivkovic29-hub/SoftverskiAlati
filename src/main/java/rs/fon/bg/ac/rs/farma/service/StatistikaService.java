package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.StatistikaFarmeResponse;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Service
public class StatistikaService {
    private final FarmaRepository farmaRepository;
    private final KravaRepository kravaRepository;
    private final BikRepository bikRepository;
    private final VeterinarRepository veterinarRepository;
    private final SteonostRepository steonostRepository;
    private final ProizvodnjaMlekaRepository mlekoRepository;

    public StatistikaService(FarmaRepository farmaRepository, KravaRepository kravaRepository,
                             BikRepository bikRepository, VeterinarRepository veterinarRepository,
                             SteonostRepository steonostRepository,
                             ProizvodnjaMlekaRepository mlekoRepository) {
        this.farmaRepository = farmaRepository;
        this.kravaRepository = kravaRepository;
        this.bikRepository = bikRepository;
        this.veterinarRepository = veterinarRepository;
        this.steonostRepository = steonostRepository;
        this.mlekoRepository = mlekoRepository;
    }

    @Transactional(readOnly = true)
    public StatistikaFarmeResponse prikazi(Long farmaId, LocalDate od, LocalDate doDatuma) {
        if (od.isAfter(doDatuma)) {
            throw new BusinessException("Pocetni datum ne moze biti posle krajnjeg");
        }
        Farma farma = farmaRepository.findById(farmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Farma nije pronadjena: " + farmaId));
        Map<StatusKrave, Long> poStatusu = new EnumMap<>(StatusKrave.class);
        for (StatusKrave status : StatusKrave.values()) {
            poStatusu.put(status, kravaRepository.countByFarmaIdAndStatus(farmaId, status));
        }
        BigDecimal mleko = mlekoRepository.ukupnoZaFarmu(farmaId, od, doDatuma);
        if (mleko == null) mleko = BigDecimal.ZERO;
        return new StatistikaFarmeResponse(farmaId, farma.getNaziv(),
                kravaRepository.countByFarmaId(farmaId), poStatusu,
                bikRepository.count(), veterinarRepository.count(),
                steonostRepository.countByKravaFarmaIdAndAktivnaTrue(farmaId),
                od, doDatuma, mleko);
    }
}
