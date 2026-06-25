package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.domain.Krava;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;
import rs.fon.bg.ac.rs.farma.dto.KravaRequest;
import rs.fon.bg.ac.rs.farma.dto.KravaResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.FarmaRepository;
import rs.fon.bg.ac.rs.farma.repository.KravaRepository;

import java.util.List;

@Service
public class KravaService {
    private final KravaRepository kravaRepository;
    private final FarmaRepository farmaRepository;

    public KravaService(KravaRepository kravaRepository, FarmaRepository farmaRepository) {
        this.kravaRepository = kravaRepository;
        this.farmaRepository = farmaRepository;
    }

    @Transactional
    public KravaResponse dodaj(KravaRequest request) {
        if (kravaRepository.existsByBrojMarkice(request.brojMarkice())) {
            throw new DuplicateResourceException("Krava sa datim brojem markice vec postoji");
        }
        Krava krava = new Krava();
        primeni(krava, request);
        return toResponse(kravaRepository.save(krava));
    }

    @Transactional
    public KravaResponse izmeni(Long id, KravaRequest request) {
        Krava krava = getEntity(id);
        if (kravaRepository.existsByBrojMarkiceAndIdNot(request.brojMarkice(), id)) {
            throw new DuplicateResourceException("Krava sa datim brojem markice vec postoji");
        }
        primeni(krava, request);
        return toResponse(kravaRepository.save(krava));
    }

    @Transactional
    public void obrisi(Long id) {
        kravaRepository.delete(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<KravaResponse> pretrazi(String brojMarkice, StatusKrave status) {
        String kriterijum = brojMarkice == null || brojMarkice.isBlank() ? null : brojMarkice.trim();
        return kravaRepository.pretrazi(kriterijum, status).stream().map(this::toResponse).toList();
    }

    public Krava getEntity(Long id) {
        return kravaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Krava nije pronadjena: " + id));
    }

    private void primeni(Krava krava, KravaRequest request) {
        Farma farma = farmaRepository.findById(request.farmaId())
                .orElseThrow(() -> new ResourceNotFoundException("Farma nije pronadjena: " + request.farmaId()));
        krava.setBrojMarkice(request.brojMarkice());
        krava.setDatumRodjenja(request.datumRodjenja());
        krava.setRasa(request.rasa());
        krava.setLaktacija(request.laktacija());
        krava.setStatus(request.status());
        krava.setFarma(farma);
    }

    private KravaResponse toResponse(Krava krava) {
        return new KravaResponse(krava.getId(), krava.getBrojMarkice(), krava.getDatumRodjenja(),
                krava.getRasa(), krava.getLaktacija(), krava.getStatus(),
                krava.getFarma().getId(), krava.getFarma().getNaziv());
    }
}
