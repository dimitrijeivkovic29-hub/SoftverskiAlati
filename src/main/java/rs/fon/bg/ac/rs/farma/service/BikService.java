package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Bik;
import rs.fon.bg.ac.rs.farma.dto.BikRequest;
import rs.fon.bg.ac.rs.farma.dto.BikResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.BikRepository;

import java.util.List;

@Service
public class BikService {
    private final BikRepository bikRepository;

    public BikService(BikRepository bikRepository) { this.bikRepository = bikRepository; }

    @Transactional
    public BikResponse dodaj(BikRequest request) {
        if (bikRepository.existsByHbBroj(request.hbBroj())) {
            throw new DuplicateResourceException("Bik sa datim HB brojem vec postoji");
        }
        Bik bik = new Bik();
        primeni(bik, request);
        return toResponse(bikRepository.save(bik));
    }

    @Transactional
    public BikResponse izmeni(Long id, BikRequest request) {
        Bik bik = getEntity(id);
        if (bikRepository.existsByHbBrojAndIdNot(request.hbBroj(), id)) {
            throw new DuplicateResourceException("Bik sa datim HB brojem vec postoji");
        }
        primeni(bik, request);
        return toResponse(bikRepository.save(bik));
    }

    @Transactional(readOnly = true)
    public List<BikResponse> svi() {
        return bikRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Bik getEntity(Long id) {
        return bikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bik nije pronadjen: " + id));
    }

    private void primeni(Bik bik, BikRequest request) {
        bik.setNaziv(request.naziv());
        bik.setHbBroj(request.hbBroj());
        bik.setRasa(request.rasa());
    }

    private BikResponse toResponse(Bik bik) {
        return new BikResponse(bik.getId(), bik.getNaziv(), bik.getHbBroj(), bik.getRasa());
    }
}
