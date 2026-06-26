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

/**
 * Servis za evidenciju veterinara koji ucestvuju u reproduktivnim postupcima.
 * Obezbedjuje dodavanje i pregled veterinara, proveru jedinstvenosti telefona
 * i pronalazenje domenskog entiteta prema identifikatoru.
 * @author Dimitrije Ivkovic
 */
@Service
public class VeterinarService {
    private final VeterinarRepository veterinarRepository;

    /**
     * Kreira servis sa potrebnim repozitorijumom.
     * @param veterinarRepository repozitorijum za pristup podacima o veterinarima
     */
    public VeterinarService(VeterinarRepository veterinarRepository) {
        this.veterinarRepository = veterinarRepository;
    }

    /**
     * Dodaje novog veterinara nakon provere jedinstvenosti telefona.
     * @param request licni i kontakt podaci veterinara
     * @return DTO sa podacima sacuvanog veterinara
     * @throws DuplicateResourceException ako vec postoji veterinar sa istim telefonom
     */
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

    /**
     * Vraca sve evidentirane veterinare.
     * @return lista DTO objekata svih veterinara; prazna lista ako nema podataka
     */
    @Transactional(readOnly = true)
    public List<VeterinarResponse> svi() {
        return veterinarRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Pronalazi domenski entitet veterinara prema identifikatoru.
     * @param id jedinstveni identifikator veterinara
     * @return pronadjeni entitet veterinara
     * @throws ResourceNotFoundException ako veterinar ne postoji
     */
    public Veterinar getEntity(Long id) {
        return veterinarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinar nije pronadjen: " + id));
    }

    /**
     * Mapira entitet veterinara u DTO odgovor.
     * @param v entitet koji se mapira
     * @return DTO sa podacima veterinara
     */
    private VeterinarResponse toResponse(Veterinar v) {
        return new VeterinarResponse(v.getId(), v.getIme(), v.getPrezime(), v.getTelefon());
    }
}
