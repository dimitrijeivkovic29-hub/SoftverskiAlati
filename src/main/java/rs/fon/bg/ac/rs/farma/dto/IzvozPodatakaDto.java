package rs.fon.bg.ac.rs.farma.dto;

import rs.fon.bg.ac.rs.farma.domain.StatusKrave;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record IzvozPodatakaDto(
        int verzija,
        LocalDateTime kreirano,
        List<FarmaStavka> farme,
        List<KravaStavka> krave,
        List<BikStavka> bikovi,
        List<VeterinarStavka> veterinari,
        List<OsemenjavanjeStavka> osemenjavanja,
        List<SteonostStavka> steonosti,
        List<TeljenjeStavka> teljenja,
        List<MlekoStavka> proizvodnjaMleka
) {
    public record FarmaStavka(Long id, String naziv, String adresa, String pib) { }
    public record KravaStavka(Long id, String brojMarkice, LocalDate datumRodjenja, String rasa,
                             int laktacija, StatusKrave status, Long farmaId) { }
    public record BikStavka(Long id, String naziv, String hbBroj, String rasa) { }
    public record VeterinarStavka(Long id, String ime, String prezime, String telefon) { }
    public record OsemenjavanjeStavka(Long id, LocalDate datum, int redniBroj, String napomena,
                                     Long kravaId, Long bikId, Long veterinarId) { }
    public record SteonostStavka(Long id, LocalDate datumPotvrde, LocalDate ocekivaniDatumTeljenja,
                                boolean aktivna, Long kravaId, Long veterinarId) { }
    public record TeljenjeStavka(Long id, LocalDate datum, int brojTeladi, String napomena,
                                Long kravaId, Long steonostId) { }
    public record MlekoStavka(Long id, LocalDate datum, BigDecimal jutarnjaLitara,
                             BigDecimal vecernjaLitara, BigDecimal ukupnoLitara, Long kravaId) { }
}
