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

/**
 * Servis za dnevnu evidenciju i periodicni pregled proizvodnje mleka.
 * Sprovodi pravila jedinstvenog unosa po kravi i datumu, zabrane unosa za izlucene
 * krave i izracunavanja zbirne i prosecne proizvodnje.
 * @author Dimitrije Ivkovic
 */
@Service
public class ProizvodnjaMlekaService {
    private final ProizvodnjaMlekaRepository repository;
    private final KravaRepository kravaRepository;

    /**
     * Kreira servis sa repozitorijumima za proizvodnju mleka i krave.
     * @param repository repozitorijum dnevnih unosa proizvodnje mleka
     * @param kravaRepository repozitorijum za pristup kravama
     */
    public ProizvodnjaMlekaService(ProizvodnjaMlekaRepository repository, KravaRepository kravaRepository) {
        this.repository = repository;
        this.kravaRepository = kravaRepository;
    }

    /**
     * Evidentira jutarnju i vecernju proizvodnju mleka za jednu kravu i datum.
     * Ukupna kolicina se automatski izracunava pre cuvanja.
     * @param request identifikator krave, datum i kolicine jutarnje i vecernje muze
     * @return DTO sa sacuvanim dnevnim unosom
     * @throws ResourceNotFoundException ako krava ne postoji
     * @throws BusinessException ako je krava izlucena iz proizvodnje
     * @throws DuplicateResourceException ako za istu kravu i datum vec postoji unos
     */
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

    /**
     * Formira pregled proizvodnje mleka za kravu u zadatom, ukljucivom periodu.
     * @param kravaId jedinstveni identifikator krave
     * @param od pocetni datum perioda, ukljucivo
     * @param doDatuma krajnji datum perioda, ukljucivo
     * @return izvestaj sa stavkama, ukupnom i prosecnom proizvodnjom
     * @throws BusinessException ako je pocetni datum posle krajnjeg
     * @throws ResourceNotFoundException ako krava ne postoji
     */
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

    /**
     * Pronalazi kravu prema identifikatoru.
     * @param id jedinstveni identifikator krave
     * @return pronadjeni entitet krave
     * @throws ResourceNotFoundException ako krava ne postoji
     */
    private Krava getKrava(Long id) {
        return kravaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Krava nije pronadjena: " + id));
    }

    /**
     * Mapira dnevni unos proizvodnje mleka u DTO odgovor.
     * @param p entitet koji se mapira
     * @return DTO sa podacima dnevne proizvodnje
     */
    private ProizvodnjaMlekaResponse toResponse(ProizvodnjaMleka p) {
        return new ProizvodnjaMlekaResponse(p.getId(), p.getKrava().getId(), p.getKrava().getBrojMarkice(),
                p.getDatum(), p.getJutarnjaLitara(), p.getVecernjaLitara(), p.getUkupnoLitara());
    }
}
