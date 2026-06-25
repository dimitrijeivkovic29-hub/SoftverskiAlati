package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Farma;
import rs.fon.bg.ac.rs.farma.dto.FarmaRequest;
import rs.fon.bg.ac.rs.farma.dto.FarmaResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.FarmaRepository;

import java.util.List;

@Service
public class FarmaService {
    private final FarmaRepository farmaRepository;

    public FarmaService(FarmaRepository farmaRepository) {
        this.farmaRepository = farmaRepository;
    }

    @Transactional
    public FarmaResponse dodaj(FarmaRequest request) {
        if (farmaRepository.existsByPib(request.pib())) {
            throw new DuplicateResourceException("Farma sa datim PIB-om vec postoji");
        }
        Farma farma = new Farma();
        primeni(farma, request);
        return toResponse(farmaRepository.save(farma));
    }

    @Transactional(readOnly = true)
    public List<FarmaResponse> sve() {
        return farmaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FarmaResponse nadji(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public FarmaResponse izmeni(Long id, FarmaRequest request) {
        Farma farma = getEntity(id);
        if (farmaRepository.existsByPibAndIdNot(request.pib(), id)) {
            throw new DuplicateResourceException("Farma sa datim PIB-om vec postoji");
        }
        primeni(farma, request);
        return toResponse(farmaRepository.save(farma));
    }

    @Transactional
    public void obrisi(Long id) {
        farmaRepository.delete(getEntity(id));
    }

    private Farma getEntity(Long id) {
        return farmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farma nije pronadjena: " + id));
    }

    private void primeni(Farma farma, FarmaRequest request) {
        farma.setNaziv(request.naziv());
        farma.setAdresa(request.adresa());
        farma.setPib(request.pib());
    }

    private FarmaResponse toResponse(Farma farma) {
        return new FarmaResponse(farma.getId(), farma.getNaziv(), farma.getAdresa(), farma.getPib());
    }
}
