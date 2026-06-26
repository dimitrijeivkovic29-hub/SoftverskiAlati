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

/**
 * Servis za upravljanje farmama mlecnih krava.
 * Obezbedjuje osnovne CRUD operacije i sprovodi poslovno pravilo jedinstvenosti PIB-a.
 * @author Dimitrije Ivkovic
 */
@Service
public class FarmaService {
    private final FarmaRepository farmaRepository;

    /**
     * Kreira servis sa potrebnim repozitorijumom.
     * @param farmaRepository repozitorijum za pristup podacima o farmama
     */
    public FarmaService(FarmaRepository farmaRepository) {
        this.farmaRepository = farmaRepository;
    }

    /**
     * Dodaje novu farmu nakon provere jedinstvenosti PIB-a.
     * @param request podaci nove farme
     * @return DTO sa podacima sacuvane farme
     * @throws DuplicateResourceException ako vec postoji farma sa istim PIB-om
     */
    @Transactional
    public FarmaResponse dodaj(FarmaRequest request) {
        if (farmaRepository.existsByPib(request.pib())) {
            throw new DuplicateResourceException("Farma sa datim PIB-om vec postoji");
        }
        Farma farma = new Farma();
        primeni(farma, request);
        return toResponse(farmaRepository.save(farma));
    }

    /**
     * Vraca sve evidentirane farme.
     * @return lista DTO objekata svih farmi; prazna lista ako nema podataka
     */
    @Transactional(readOnly = true)
    public List<FarmaResponse> sve() {
        return farmaRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Pronalazi farmu prema identifikatoru.
     * @param id jedinstveni identifikator farme
     * @return DTO sa podacima pronadjene farme
     * @throws ResourceNotFoundException ako farma ne postoji
     */
    @Transactional(readOnly = true)
    public FarmaResponse nadji(Long id) {
        return toResponse(getEntity(id));
    }

    /**
     * Menja podatke postojece farme i ponovo proverava jedinstvenost PIB-a.
     * @param id jedinstveni identifikator farme
     * @param request novi podaci farme
     * @return DTO sa izmenjenim podacima farme
     * @throws ResourceNotFoundException ako farma ne postoji
     * @throws DuplicateResourceException ako drugi zapis vec koristi prosledjeni PIB
     */
    @Transactional
    public FarmaResponse izmeni(Long id, FarmaRequest request) {
        Farma farma = getEntity(id);
        if (farmaRepository.existsByPibAndIdNot(request.pib(), id)) {
            throw new DuplicateResourceException("Farma sa datim PIB-om vec postoji");
        }
        primeni(farma, request);
        return toResponse(farmaRepository.save(farma));
    }

    /**
     * Brise farmu sa zadatim identifikatorom.
     * Zbog kaskadnih veza brisu se i krave koje pripadaju farmi i njihove zavisne evidencije.
     * @param id jedinstveni identifikator farme
     * @throws ResourceNotFoundException ako farma ne postoji
     */
    @Transactional
    public void obrisi(Long id) {
        farmaRepository.delete(getEntity(id));
    }

    /**
     * Pronalazi domenski entitet farme prema identifikatoru.
     * @param id jedinstveni identifikator farme
     * @return pronadjeni entitet farme
     * @throws ResourceNotFoundException ako farma ne postoji
     */
    private Farma getEntity(Long id) {
        return farmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farma nije pronadjena: " + id));
    }

    /**
     * Prenosi vrednosti iz DTO zahteva na entitet farme.
     * @param farma entitet koji se popunjava
     * @param request izvor novih vrednosti
     */
    private void primeni(Farma farma, FarmaRequest request) {
        farma.setNaziv(request.naziv());
        farma.setAdresa(request.adresa());
        farma.setPib(request.pib());
    }

    /**
     * Mapira entitet farme u DTO odgovor.
     * @param farma entitet koji se mapira
     * @return DTO sa podacima farme
     */
    private FarmaResponse toResponse(Farma farma) {
        return new FarmaResponse(farma.getId(), farma.getNaziv(), farma.getAdresa(), farma.getPib());
    }
}
