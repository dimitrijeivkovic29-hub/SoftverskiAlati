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

/**
 * Servis za upravljanje kravama i njihovo povezivanje sa farmama.
 * Obezbedjuje dodavanje, izmenu, brisanje i pretragu krava uz proveru jedinstvenosti
 * broja markice i postojanja izabrane farme.
 * @author Dimitrije Ivkovic
 */
@Service
public class KravaService {
    private final KravaRepository kravaRepository;
    private final FarmaRepository farmaRepository;

    /**
     * Kreira servis sa repozitorijumima za krave i farme.
     * @param kravaRepository repozitorijum za pristup podacima o kravama
     * @param farmaRepository repozitorijum za pristup podacima o farmama
     */
    public KravaService(KravaRepository kravaRepository, FarmaRepository farmaRepository) {
        this.kravaRepository = kravaRepository;
        this.farmaRepository = farmaRepository;
    }

    /**
     * Dodaje novu kravu i povezuje je sa postojecom farmom.
     * @param request podaci krave i identifikator farme kojoj pripada
     * @return DTO sa podacima sacuvane krave
     * @throws DuplicateResourceException ako vec postoji krava sa istim brojem markice
     * @throws ResourceNotFoundException ako izabrana farma ne postoji
     */
    @Transactional
    public KravaResponse dodaj(KravaRequest request) {
        if (kravaRepository.existsByBrojMarkice(request.brojMarkice())) {
            throw new DuplicateResourceException("Krava sa datim brojem markice vec postoji");
        }
        Krava krava = new Krava();
        primeni(krava, request);
        return toResponse(kravaRepository.save(krava));
    }

    /**
     * Menja podatke postojece krave i njenu pripadnost farmi.
     * @param id jedinstveni identifikator krave
     * @param request novi podaci krave
     * @return DTO sa izmenjenim podacima krave
     * @throws ResourceNotFoundException ako krava ili izabrana farma ne postoji
     * @throws DuplicateResourceException ako druga krava vec koristi prosledjeni broj markice
     */
    @Transactional
    public KravaResponse izmeni(Long id, KravaRequest request) {
        Krava krava = getEntity(id);
        if (kravaRepository.existsByBrojMarkiceAndIdNot(request.brojMarkice(), id)) {
            throw new DuplicateResourceException("Krava sa datim brojem markice vec postoji");
        }
        primeni(krava, request);
        return toResponse(kravaRepository.save(krava));
    }

    /**
     * Brise kravu i zavisne evidencije koje podlezu kaskadnom brisanju.
     * @param id jedinstveni identifikator krave
     * @throws ResourceNotFoundException ako krava ne postoji
     */
    @Transactional
    public void obrisi(Long id) {
        kravaRepository.delete(getEntity(id));
    }

    /**
     * Pretrazuje krave prema delu broja markice i opcionom statusu.
     * @param brojMarkice deo broja markice; null ili prazan tekst znaci da se kriterijum ignorise
     * @param status trazeni status; null znaci da se status ne koristi kao kriterijum
     * @return lista krava koje ispunjavaju prosledjene kriterijume
     */
    @Transactional(readOnly = true)
    public List<KravaResponse> pretrazi(String brojMarkice, StatusKrave status) {
        String kriterijum = brojMarkice == null || brojMarkice.isBlank() ? null : brojMarkice.trim();
        return kravaRepository.pretrazi(kriterijum, status).stream().map(this::toResponse).toList();
    }

    /**
     * Pronalazi domenski entitet krave prema identifikatoru.
     * @param id jedinstveni identifikator krave
     * @return pronadjeni entitet krave
     * @throws ResourceNotFoundException ako krava ne postoji
     */
    public Krava getEntity(Long id) {
        return kravaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Krava nije pronadjena: " + id));
    }

    /**
     * Prenosi vrednosti iz DTO zahteva na entitet krave i povezuje ga sa farmom.
     * @param krava entitet koji se popunjava
     * @param request izvor novih vrednosti i identifikatora farme
     * @throws ResourceNotFoundException ako izabrana farma ne postoji
     */
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

    /**
     * Mapira entitet krave u DTO odgovor.
     * @param krava entitet koji se mapira
     * @return DTO sa podacima krave i njene farme
     */
    private KravaResponse toResponse(Krava krava) {
        return new KravaResponse(krava.getId(), krava.getBrojMarkice(), krava.getDatumRodjenja(),
                krava.getRasa(), krava.getLaktacija(), krava.getStatus(),
                krava.getFarma().getId(), krava.getFarma().getNaziv());
    }
}
