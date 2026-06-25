package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.Veterinar;
import rs.fon.bg.ac.rs.farma.dto.VeterinarRequest;
import rs.fon.bg.ac.rs.farma.dto.VeterinarResponse;
import rs.fon.bg.ac.rs.farma.exception.DuplicateResourceException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.VeterinarRepository;

import java.util.List;

@Service
public class VeterinarService {
    private final VeterinarRepository veterinarRepository;

    public VeterinarService(VeterinarRepository veterinarRepository) {
        this.veterinarRepository = veterinarRepository;
    }

    @Transactional
    public VeterinarResponse dodaj(VeterinarRequest request) {
        if (veterinarRepository.existsByTelefon(request.telefon())) {
            throw new DuplicateResourceException("Veterinar sa datim telefonom vec postoji");
        }
        Veterinar veterinar = new Veterinar();
        veterinar.setIme(request.ime());
        veterinar.setPrezime(request.prezime());
        veterinar.setTelefon(request.telefon());
        return toResponse(veterinarRepository.save(veterinar));
    }

    @Transactional(readOnly = true)
    public List<VeterinarResponse> svi() {
        return veterinarRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Veterinar getEntity(Long id) {
        return veterinarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinar nije pronadjen: " + id));
    }

    private VeterinarResponse toResponse(Veterinar v) {
        return new VeterinarResponse(v.getId(), v.getIme(), v.getPrezime(), v.getTelefon());
    }
}
