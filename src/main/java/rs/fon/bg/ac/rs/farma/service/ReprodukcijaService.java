package rs.fon.bg.ac.rs.farma.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.*;
import rs.fon.bg.ac.rs.farma.dto.*;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.exception.ResourceNotFoundException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Servis koji sprovodi poslovna pravila reproduktivnog ciklusa krave.
 * Obuhvata osemenjavanje, izbor krava za proveru, potvrdu steonosti i teljenje,
 * uz automatsko menjanje statusa krave i broja laktacije.
 * @author Dimitrije Ivkovic
 */
@Service
public class ReprodukcijaService {
    private static final int PROSECNA_STEONOST_DANA = 280;

    private final KravaRepository kravaRepository;
    private final BikRepository bikRepository;
    private final VeterinarRepository veterinarRepository;
    private final OsemenjavanjeRepository osemenjavanjeRepository;
    private final SteonostRepository steonostRepository;
    private final TeljenjeRepository teljenjeRepository;

    /**
     * Kreira servis sa repozitorijumima potrebnim za reproduktivni ciklus.
     * @param kravaRepository repozitorijum krava
     * @param bikRepository repozitorijum bikova
     * @param veterinarRepository repozitorijum veterinara
     * @param osemenjavanjeRepository repozitorijum osemenjavanja
     * @param steonostRepository repozitorijum steonosti
     * @param teljenjeRepository repozitorijum teljenja
     */
    public ReprodukcijaService(KravaRepository kravaRepository,
                               BikRepository bikRepository,
                               VeterinarRepository veterinarRepository,
                               OsemenjavanjeRepository osemenjavanjeRepository,
                               SteonostRepository steonostRepository,
                               TeljenjeRepository teljenjeRepository) {
        this.kravaRepository = kravaRepository;
        this.bikRepository = bikRepository;
        this.veterinarRepository = veterinarRepository;
        this.osemenjavanjeRepository = osemenjavanjeRepository;
        this.steonostRepository = steonostRepository;
        this.teljenjeRepository = teljenjeRepository;
    }

    /**
     * Evidentira novo osemenjavanje i postavlja kravu u status za proveru steonosti.
     * Redni broj se odredjuje kao broj prethodnih osemenjavanja krave uvecan za jedan.
     * @param request podaci o kravi, biku, veterinaru, datumu i opcionoj napomeni
     * @return DTO sa podacima sacuvanog osemenjavanja
     * @throws ResourceNotFoundException ako krava, bik ili veterinar ne postoji
     * @throws BusinessException ako je krava izlucena ili je datum pre njenog rodjenja
     */
    @Transactional
    public OsemenjavanjeResponse evidentirajOsemenjavanje(OsemenjavanjeRequest request) {
        Krava krava = getKrava(request.kravaId());
        if (krava.getStatus() == StatusKrave.IZLUCENA) {
            throw new BusinessException("Izlucena krava ne moze biti osemenjena");
        }
        if (request.datum().isBefore(krava.getDatumRodjenja())) {
            throw new BusinessException("Datum osemenjavanja ne moze biti pre rodjenja krave");
        }
        Bik bik = bikRepository.findById(request.bikId())
                .orElseThrow(() -> new ResourceNotFoundException("Bik nije pronadjen: " + request.bikId()));
        Veterinar veterinar = getVeterinar(request.veterinarId());

        Osemenjavanje entity = new Osemenjavanje();
        entity.setKrava(krava);
        entity.setBik(bik);
        entity.setVeterinar(veterinar);
        entity.setDatum(request.datum());
        entity.setRedniBroj((int) osemenjavanjeRepository.countByKravaId(krava.getId()) + 1);
        entity.setNapomena(request.napomena());
        krava.setStatus(StatusKrave.ZA_PROVERU_STEONOSTI);
        kravaRepository.save(krava);
        return toResponse(osemenjavanjeRepository.save(entity));
    }

    /**
     * Vraca istoriju osemenjavanja krave od najnovijeg ka najstarijem.
     * @param kravaId jedinstveni identifikator krave
     * @return uredjena lista evidentiranih osemenjavanja
     * @throws ResourceNotFoundException ako krava ne postoji
     */
    @Transactional(readOnly = true)
    public List<OsemenjavanjeResponse> istorijaOsemenjavanja(Long kravaId) {
        getKrava(kravaId);
        return osemenjavanjeRepository.findByKravaIdOrderByDatumDescRedniBrojDesc(kravaId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Vraca krave kod kojih je od poslednjeg osemenjavanja prosao minimalni broj dana.
     * Rezultat je sortiran opadajuce prema broju dana od poslednjeg osemenjavanja.
     * @param minimalniBrojDana minimalni broj dana; mora biti veci ili jednak jedan
     * @return lista krava spremnih za proveru steonosti
     * @throws BusinessException ako je minimalni broj dana manji od jedan
     */
    @Transactional(readOnly = true)
    public List<ProveraSteonostiResponse> kraveZaProveru(int minimalniBrojDana) {
        if (minimalniBrojDana < 1) {
            throw new BusinessException("Minimalni broj dana mora biti pozitivan");
        }
        LocalDate danas = LocalDate.now();
        return kravaRepository.findByStatus(StatusKrave.ZA_PROVERU_STEONOSTI).stream()
                .map(krava -> osemenjavanjeRepository.findTopByKravaIdOrderByDatumDescRedniBrojDesc(krava.getId())
                        .map(o -> new ProveraSteonostiResponse(krava.getId(), krava.getBrojMarkice(), o.getDatum(),
                                ChronoUnit.DAYS.between(o.getDatum(), danas)))
                        .orElse(null))
                .filter(item -> item != null && item.danaOdOsemenjavanja() >= minimalniBrojDana)
                .sorted(Comparator.comparing(ProveraSteonostiResponse::danaOdOsemenjavanja).reversed())
                .toList();
    }

    /**
     * Potvrdjuje steonost na osnovu poslednjeg osemenjavanja i racuna ocekivani datum teljenja.
     * Ocekivani datum se racuna dodavanjem 280 dana na datum poslednjeg osemenjavanja,
     * a status krave se menja u STEONA.
     * @param request identifikatori krave i veterinara i datum potvrde
     * @return DTO sa podacima sacuvane steonosti
     * @throws ResourceNotFoundException ako krava ili veterinar ne postoji
     * @throws BusinessException ako krava vec ima aktivnu steonost, nema osemenjavanje
     * ili je datum potvrde pre poslednjeg osemenjavanja
     */
    @Transactional
    public SteonostResponse potvrdiSteonost(PotvrdaSteonostiRequest request) {
        Krava krava = getKrava(request.kravaId());
        if (steonostRepository.findFirstByKravaIdAndAktivnaTrue(krava.getId()).isPresent()) {
            throw new BusinessException("Krava vec ima aktivnu steonost");
        }
        Osemenjavanje poslednje = osemenjavanjeRepository
                .findTopByKravaIdOrderByDatumDescRedniBrojDesc(krava.getId())
                .orElseThrow(() -> new BusinessException("Krava nema evidentirano osemenjavanje"));
        if (request.datumPotvrde().isBefore(poslednje.getDatum())) {
            throw new BusinessException("Potvrda steonosti ne moze biti pre osemenjavanja");
        }
        Veterinar veterinar = getVeterinar(request.veterinarId());
        Steonost steonost = new Steonost();
        steonost.setKrava(krava);
        steonost.setVeterinar(veterinar);
        steonost.setDatumPotvrde(request.datumPotvrde());
        steonost.setOcekivaniDatumTeljenja(poslednje.getDatum().plusDays(PROSECNA_STEONOST_DANA));
        steonost.setAktivna(true);
        krava.setStatus(StatusKrave.STEONA);
        kravaRepository.save(krava);
        return toResponse(steonostRepository.save(steonost));
    }

    /**
     * Evidentira teljenje i zatvara aktivnu steonost krave.
     * Nakon teljenja broj laktacije se uvecava za jedan, a status krave postaje U_LAKTACIJI.
     * @param request identifikator krave, datum, broj teladi i opciona napomena
     * @return DTO sa podacima sacuvanog teljenja
     * @throws ResourceNotFoundException ako krava ne postoji
     * @throws BusinessException ako krava nema aktivnu steonost ili je datum teljenja
     * pre datuma potvrde steonosti
     */
    @Transactional
    public TeljenjeResponse evidentirajTeljenje(TeljenjeRequest request) {
        Krava krava = getKrava(request.kravaId());
        Steonost steonost = steonostRepository.findFirstByKravaIdAndAktivnaTrue(krava.getId())
                .orElseThrow(() -> new BusinessException("Krava nema aktivnu steonost"));
        if (request.datum().isBefore(steonost.getDatumPotvrde())) {
            throw new BusinessException("Datum teljenja ne moze biti pre potvrde steonosti");
        }
        Teljenje teljenje = new Teljenje();
        teljenje.setKrava(krava);
        teljenje.setSteonost(steonost);
        teljenje.setDatum(request.datum());
        teljenje.setBrojTeladi(request.brojTeladi());
        teljenje.setNapomena(request.napomena());

        steonost.setAktivna(false);
        steonost.setTeljenje(teljenje);
        krava.setLaktacija(krava.getLaktacija() + 1);
        krava.setStatus(StatusKrave.U_LAKTACIJI);
        kravaRepository.save(krava);
        steonostRepository.save(steonost);
        return toResponse(teljenjeRepository.save(teljenje));
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
     * Pronalazi veterinara prema identifikatoru.
     * @param id jedinstveni identifikator veterinara
     * @return pronadjeni entitet veterinara
     * @throws ResourceNotFoundException ako veterinar ne postoji
     */
    private Veterinar getVeterinar(Long id) {
        return veterinarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinar nije pronadjen: " + id));
    }

    /**
     * Mapira osemenjavanje u DTO odgovor.
     * @param o entitet koji se mapira
     * @return DTO sa podacima osemenjavanja i povezanih entiteta
     */
    private OsemenjavanjeResponse toResponse(Osemenjavanje o) {
        return new OsemenjavanjeResponse(o.getId(), o.getDatum(), o.getRedniBroj(), o.getNapomena(),
                o.getKrava().getId(), o.getKrava().getBrojMarkice(),
                o.getBik().getId(), o.getBik().getNaziv(),
                o.getVeterinar().getId(), o.getVeterinar().getIme() + " " + o.getVeterinar().getPrezime());
    }

    /**
     * Mapira steonost u DTO odgovor.
     * @param s entitet koji se mapira
     * @return DTO sa podacima steonosti, krave i veterinara
     */
    private SteonostResponse toResponse(Steonost s) {
        return new SteonostResponse(s.getId(), s.getKrava().getId(), s.getKrava().getBrojMarkice(),
                s.getDatumPotvrde(), s.getOcekivaniDatumTeljenja(), s.isAktivna(),
                s.getVeterinar().getId(), s.getVeterinar().getIme() + " " + s.getVeterinar().getPrezime());
    }

    /**
     * Mapira teljenje u DTO odgovor.
     * @param t entitet koji se mapira
     * @return DTO sa podacima teljenja, krave i steonosti
     */
    private TeljenjeResponse toResponse(Teljenje t) {
        return new TeljenjeResponse(t.getId(), t.getKrava().getId(), t.getKrava().getBrojMarkice(),
                t.getSteonost().getId(), t.getDatum(), t.getBrojTeladi(), t.getNapomena());
    }
}
