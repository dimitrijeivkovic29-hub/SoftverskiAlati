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

/**
 * Servis za upravljanje bikovima koji se koriste u reproduktivnim postupcima.
 * Obezbedjuje dodavanje, izmenu i pregled bikova, proverava jedinstvenost HB broja
 * i mapira domenske entitete u DTO odgovore.
 * @author Dimitrije Ivkovic
 */
@Service
public class BikService {
    private final BikRepository bikRepository;

    /**
     * Kreira servis sa potrebnim repozitorijumom.
     * @param bikRepository repozitorijum za pristup podacima o bikovima
     */
    public BikService(BikRepository bikRepository) { this.bikRepository = bikRepository; }

    /**
     * Dodaje novog bika nakon provere jedinstvenosti HB broja.
     * @param request podaci novog bika; ne smeju biti null i moraju zadovoljiti validaciona ogranicenja DTO-a
     * @return DTO sa podacima sacuvanog bika
     * @throws DuplicateResourceException ako vec postoji bik sa istim HB brojem
     */
    @Transactional
    public BikResponse dodaj(BikRequest request) {
        if (bikRepository.existsByHbBroj(request.hbBroj())) {
            throw new DuplicateResourceException("Bik sa datim HB brojem vec postoji");
        }
        Bik bik = new Bik();
        primeni(bik, request);
        return toResponse(bikRepository.save(bik));
    }

    /**
     * Menja podatke postojeceg bika i ponovo proverava jedinstvenost HB broja.
     * @param id jedinstveni identifikator bika koji se menja
     * @param request novi podaci bika
     * @return DTO sa izmenjenim podacima bika
     * @throws ResourceNotFoundException ako bik sa zadatim identifikatorom ne postoji
     * @throws DuplicateResourceException ako drugi bik vec koristi prosledjeni HB broj
     */
    @Transactional
    public BikResponse izmeni(Long id, BikRequest request) {
        Bik bik = getEntity(id);
        if (bikRepository.existsByHbBrojAndIdNot(request.hbBroj(), id)) {
            throw new DuplicateResourceException("Bik sa datim HB brojem vec postoji");
        }
        primeni(bik, request);
        return toResponse(bikRepository.save(bik));
    }

    /**
     * Vraca sve evidentirane bikove.
     * @return lista DTO objekata svih bikova; prazna lista ako nema podataka
     */
    @Transactional(readOnly = true)
    public List<BikResponse> svi() {
        return bikRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Pronalazi domenski entitet bika prema identifikatoru.
     * @param id jedinstveni identifikator bika
     * @return pronadjeni entitet bika
     * @throws ResourceNotFoundException ako bik sa zadatim identifikatorom ne postoji
     */
    public Bik getEntity(Long id) {
        return bikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bik nije pronadjen: " + id));
    }

    /**
     * Prenosi vrednosti iz DTO zahteva na entitet bika.
     * @param bik entitet koji se popunjava
     * @param request izvor novih vrednosti
     */
    private void primeni(Bik bik, BikRequest request) {
        bik.setNaziv(request.naziv());
        bik.setHbBroj(request.hbBroj());
        bik.setRasa(request.rasa());
    }

    /**
     * Mapira entitet bika u DTO odgovor.
     * @param bik entitet koji se mapira
     * @return DTO sa podacima bika
     */
    private BikResponse toResponse(Bik bik) {
        return new BikResponse(bik.getId(), bik.getNaziv(), bik.getHbBroj(), bik.getRasa());
    }
}
