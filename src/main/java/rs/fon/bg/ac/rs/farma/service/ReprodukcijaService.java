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

@Service
public class ReprodukcijaService {
    private static final int PROSECNA_STEONOST_DANA = 280;

    private final KravaRepository kravaRepository;
    private final BikRepository bikRepository;
    private final VeterinarRepository veterinarRepository;
    private final OsemenjavanjeRepository osemenjavanjeRepository;
    private final SteonostRepository steonostRepository;
    private final TeljenjeRepository teljenjeRepository;

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

    @Transactional(readOnly = true)
    public List<OsemenjavanjeResponse> istorijaOsemenjavanja(Long kravaId) {
        getKrava(kravaId);
        return osemenjavanjeRepository.findByKravaIdOrderByDatumDescRedniBrojDesc(kravaId)
                .stream().map(this::toResponse).toList();
    }

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

    private Krava getKrava(Long id) {
        return kravaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Krava nije pronadjena: " + id));
    }

    private Veterinar getVeterinar(Long id) {
        return veterinarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinar nije pronadjen: " + id));
    }

    private OsemenjavanjeResponse toResponse(Osemenjavanje o) {
        return new OsemenjavanjeResponse(o.getId(), o.getDatum(), o.getRedniBroj(), o.getNapomena(),
                o.getKrava().getId(), o.getKrava().getBrojMarkice(),
                o.getBik().getId(), o.getBik().getNaziv(),
                o.getVeterinar().getId(), o.getVeterinar().getIme() + " " + o.getVeterinar().getPrezime());
    }

    private SteonostResponse toResponse(Steonost s) {
        return new SteonostResponse(s.getId(), s.getKrava().getId(), s.getKrava().getBrojMarkice(),
                s.getDatumPotvrde(), s.getOcekivaniDatumTeljenja(), s.isAktivna(),
                s.getVeterinar().getId(), s.getVeterinar().getIme() + " " + s.getVeterinar().getPrezime());
    }

    private TeljenjeResponse toResponse(Teljenje t) {
        return new TeljenjeResponse(t.getId(), t.getKrava().getId(), t.getKrava().getBrojMarkice(),
                t.getSteonost().getId(), t.getDatum(), t.getBrojTeladi(), t.getNapomena());
    }
}
