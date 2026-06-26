package rs.fon.bg.ac.rs.farma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.fon.bg.ac.rs.farma.domain.*;
import rs.fon.bg.ac.rs.farma.dto.IzvozPodatakaDto;
import rs.fon.bg.ac.rs.farma.dto.UvozRezultatResponse;
import rs.fon.bg.ac.rs.farma.exception.BusinessException;
import rs.fon.bg.ac.rs.farma.repository.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servis za izvoz i uvoz kompletnog skupa podataka aplikacije u JSON formatu.
 * Prilikom uvoza ponovo uspostavlja veze izmedju entiteta, podrzava samo verziju
 * formata 1 i dozvoljava uvoz iskljucivo u praznu bazu.
 * @author Dimitrije Ivkovic
 */
@Service
public class JsonService {
    private final ObjectMapper objectMapper;
    private final FarmaRepository farmaRepository;
    private final KravaRepository kravaRepository;
    private final BikRepository bikRepository;
    private final VeterinarRepository veterinarRepository;
    private final OsemenjavanjeRepository osemenjavanjeRepository;
    private final SteonostRepository steonostRepository;
    private final TeljenjeRepository teljenjeRepository;
    private final ProizvodnjaMlekaRepository mlekoRepository;

    /**
     * Kreira servis sa objektom za JSON obradu i svim potrebnim repozitorijumima.
     * @param objectMapper komponenta za serijalizaciju i deserijalizaciju JSON-a
     * @param farmaRepository repozitorijum farmi
     * @param kravaRepository repozitorijum krava
     * @param bikRepository repozitorijum bikova
     * @param veterinarRepository repozitorijum veterinara
     * @param osemenjavanjeRepository repozitorijum osemenjavanja
     * @param steonostRepository repozitorijum steonosti
     * @param teljenjeRepository repozitorijum teljenja
     * @param mlekoRepository repozitorijum proizvodnje mleka
     */
    public JsonService(ObjectMapper objectMapper, FarmaRepository farmaRepository,
                       KravaRepository kravaRepository, BikRepository bikRepository,
                       VeterinarRepository veterinarRepository,
                       OsemenjavanjeRepository osemenjavanjeRepository,
                       SteonostRepository steonostRepository, TeljenjeRepository teljenjeRepository,
                       ProizvodnjaMlekaRepository mlekoRepository) {
        this.objectMapper = objectMapper;
        this.farmaRepository = farmaRepository;
        this.kravaRepository = kravaRepository;
        this.bikRepository = bikRepository;
        this.veterinarRepository = veterinarRepository;
        this.osemenjavanjeRepository = osemenjavanjeRepository;
        this.steonostRepository = steonostRepository;
        this.teljenjeRepository = teljenjeRepository;
        this.mlekoRepository = mlekoRepository;
    }

    /**
     * Serijalizuje kompletne podatke aplikacije u JSON dokument verzije 1.
     * @return formatiran JSON sadrzaj kao niz bajtova
     * @throws BusinessException ako serijalizacija podataka ne uspe
     */
    @Transactional(readOnly = true)
    public byte[] izvezi() {
        IzvozPodatakaDto dto = new IzvozPodatakaDto(1, LocalDateTime.now(),
                farmaRepository.findAll().stream().map(f -> new IzvozPodatakaDto.FarmaStavka(
                        f.getId(), f.getNaziv(), f.getAdresa(), f.getPib())).toList(),
                kravaRepository.findAll().stream().map(k -> new IzvozPodatakaDto.KravaStavka(
                        k.getId(), k.getBrojMarkice(), k.getDatumRodjenja(), k.getRasa(),
                        k.getLaktacija(), k.getStatus(), k.getFarma().getId())).toList(),
                bikRepository.findAll().stream().map(b -> new IzvozPodatakaDto.BikStavka(
                        b.getId(), b.getNaziv(), b.getHbBroj(), b.getRasa())).toList(),
                veterinarRepository.findAll().stream().map(v -> new IzvozPodatakaDto.VeterinarStavka(
                        v.getId(), v.getIme(), v.getPrezime(), v.getTelefon())).toList(),
                osemenjavanjeRepository.findAll().stream().map(o -> new IzvozPodatakaDto.OsemenjavanjeStavka(
                        o.getId(), o.getDatum(), o.getRedniBroj(), o.getNapomena(), o.getKrava().getId(),
                        o.getBik().getId(), o.getVeterinar().getId())).toList(),
                steonostRepository.findAll().stream().map(s -> new IzvozPodatakaDto.SteonostStavka(
                        s.getId(), s.getDatumPotvrde(), s.getOcekivaniDatumTeljenja(), s.isAktivna(),
                        s.getKrava().getId(), s.getVeterinar().getId())).toList(),
                teljenjeRepository.findAll().stream().map(t -> new IzvozPodatakaDto.TeljenjeStavka(
                        t.getId(), t.getDatum(), t.getBrojTeladi(), t.getNapomena(), t.getKrava().getId(),
                        t.getSteonost().getId())).toList(),
                mlekoRepository.findAll().stream().map(m -> new IzvozPodatakaDto.MlekoStavka(
                        m.getId(), m.getDatum(), m.getJutarnjaLitara(), m.getVecernjaLitara(),
                        m.getUkupnoLitara(), m.getKrava().getId())).toList());
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dto);
        } catch (Exception ex) {
            throw new BusinessException("Neuspesan izvoz JSON podataka");
        }
    }

    /**
     * Uvozi podatke iz JSON dokumenta verzije 1 u praznu bazu i ponovo uspostavlja veze entiteta.
     * @param json JSON sadrzaj kao niz bajtova; ne sme predstavljati neispravan ili nepodrzan format
     * @return broj uvezenih objekata po tipu
     * @throws BusinessException ako baza nije prazna, JSON nije ispravan, verzija nije podrzana
     * ili dokument referencira objekat koji ne postoji
     */
    @Transactional
    public UvozRezultatResponse uvezi(byte[] json) {
        proveriPraznuBazu();
        final IzvozPodatakaDto dto;
        try {
            dto = objectMapper.readValue(json, IzvozPodatakaDto.class);
        } catch (Exception ex) {
            throw new BusinessException("JSON dokument nije ispravan");
        }
        if (dto.verzija() != 1) {
            throw new BusinessException("Nepodrzana verzija JSON formata: " + dto.verzija());
        }

        Map<Long, Farma> farme = new HashMap<>();
        for (IzvozPodatakaDto.FarmaStavka item : safe(dto.farme())) {
            Farma e = new Farma();
            e.setNaziv(item.naziv()); e.setAdresa(item.adresa()); e.setPib(item.pib());
            farme.put(item.id(), farmaRepository.save(e));
        }
        Map<Long, Bik> bikovi = new HashMap<>();
        for (IzvozPodatakaDto.BikStavka item : safe(dto.bikovi())) {
            Bik e = new Bik();
            e.setNaziv(item.naziv()); e.setHbBroj(item.hbBroj()); e.setRasa(item.rasa());
            bikovi.put(item.id(), bikRepository.save(e));
        }
        Map<Long, Veterinar> veterinari = new HashMap<>();
        for (IzvozPodatakaDto.VeterinarStavka item : safe(dto.veterinari())) {
            Veterinar e = new Veterinar();
            e.setIme(item.ime()); e.setPrezime(item.prezime()); e.setTelefon(item.telefon());
            veterinari.put(item.id(), veterinarRepository.save(e));
        }
        Map<Long, Krava> krave = new HashMap<>();
        for (IzvozPodatakaDto.KravaStavka item : safe(dto.krave())) {
            Krava e = new Krava();
            e.setBrojMarkice(item.brojMarkice()); e.setDatumRodjenja(item.datumRodjenja());
            e.setRasa(item.rasa()); e.setLaktacija(item.laktacija()); e.setStatus(item.status());
            e.setFarma(required(farme, item.farmaId(), "farma"));
            krave.put(item.id(), kravaRepository.save(e));
        }
        for (IzvozPodatakaDto.OsemenjavanjeStavka item : safe(dto.osemenjavanja())) {
            Osemenjavanje e = new Osemenjavanje();
            e.setDatum(item.datum()); e.setRedniBroj(item.redniBroj()); e.setNapomena(item.napomena());
            e.setKrava(required(krave, item.kravaId(), "krava"));
            e.setBik(required(bikovi, item.bikId(), "bik"));
            e.setVeterinar(required(veterinari, item.veterinarId(), "veterinar"));
            osemenjavanjeRepository.save(e);
        }
        Map<Long, Steonost> steonosti = new HashMap<>();
        for (IzvozPodatakaDto.SteonostStavka item : safe(dto.steonosti())) {
            Steonost e = new Steonost();
            e.setDatumPotvrde(item.datumPotvrde());
            e.setOcekivaniDatumTeljenja(item.ocekivaniDatumTeljenja());
            e.setAktivna(item.aktivna());
            e.setKrava(required(krave, item.kravaId(), "krava"));
            e.setVeterinar(required(veterinari, item.veterinarId(), "veterinar"));
            steonosti.put(item.id(), steonostRepository.save(e));
        }
        for (IzvozPodatakaDto.TeljenjeStavka item : safe(dto.teljenja())) {
            Teljenje e = new Teljenje();
            e.setDatum(item.datum()); e.setBrojTeladi(item.brojTeladi()); e.setNapomena(item.napomena());
            e.setKrava(required(krave, item.kravaId(), "krava"));
            Steonost steonost = required(steonosti, item.steonostId(), "steonost");
            e.setSteonost(steonost);
            Teljenje sacuvano = teljenjeRepository.save(e);
            steonost.setTeljenje(sacuvano);
            steonostRepository.save(steonost);
        }
        for (IzvozPodatakaDto.MlekoStavka item : safe(dto.proizvodnjaMleka())) {
            ProizvodnjaMleka e = new ProizvodnjaMleka();
            e.setDatum(item.datum()); e.setJutarnjaLitara(item.jutarnjaLitara());
            e.setVecernjaLitara(item.vecernjaLitara()); e.izracunajUkupno();
            e.setKrava(required(krave, item.kravaId(), "krava"));
            mlekoRepository.save(e);
        }

        return new UvozRezultatResponse(safe(dto.farme()).size(), safe(dto.krave()).size(),
                safe(dto.bikovi()).size(), safe(dto.veterinari()).size(),
                safe(dto.osemenjavanja()).size(), safe(dto.steonosti()).size(),
                safe(dto.teljenja()).size(), safe(dto.proizvodnjaMleka()).size());
    }

    /**
     * Proverava da li su sve tabele koje ucestvuju u uvozu prazne.
     * @throws BusinessException ako u bilo kom repozitorijumu vec postoje podaci
     */
    private void proveriPraznuBazu() {
        long ukupno = farmaRepository.count() + kravaRepository.count() + bikRepository.count()
                + veterinarRepository.count() + osemenjavanjeRepository.count()
                + steonostRepository.count() + teljenjeRepository.count() + mlekoRepository.count();
        if (ukupno > 0) {
            throw new BusinessException("Uvoz je dozvoljen samo u praznu bazu");
        }
    }

    /**
     * Pronalazi obavezni objekat u mapi prema identifikatoru iz uvoznog dokumenta.
     * @param map mapa originalnih identifikatora na sacuvane objekte
     * @param id originalni identifikator trazenog objekta
     * @param naziv naziv tipa objekta koji se koristi u poruci greske
     * @param <T> tip trazenog objekta
     * @return objekat povezan sa prosledjenim identifikatorom
     * @throws BusinessException ako mapa ne sadrzi trazeni identifikator
     */
    private <T> T required(Map<Long, T> map, Long id, String naziv) {
        T value = map.get(id);
        if (value == null) throw new BusinessException("JSON referencira nepostojeci objekat: " + naziv + " " + id);
        return value;
    }

    /**
     * Vraca prosledjenu listu ili praznu nepromenljivu listu kada je vrednost null.
     * @param list lista koja moze biti null
     * @param <T> tip elemenata liste
     * @return originalna lista ili prazna lista
     */
    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
