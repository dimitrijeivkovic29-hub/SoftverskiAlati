package rs.fon.bg.ac.rs.farma.domain;

/**
 * Definise moguce statuse krave kroz zivotni, reproduktivni i proizvodni ciklus.
 * Status odredjuje koje poslovne operacije je moguce izvrsiti nad kravom.
 * @author Dimitrije Ivkovic
 */
public enum StatusKrave {
    /**
     * Mlada krava koja se jos nije telila.
     */
    JUNICA,
    /**
     * Krava koja je spremna za osemenjavanje.
     */
    ZA_OSEMENJAVANJE,
    /**
     * Krava kod koje je nakon osemenjavanja potrebno proveriti steonost.
     */
    ZA_PROVERU_STEONOSTI,
    /**
     * Krava sa potvrdjenom aktivnom steonoscu.
     */
    STEONA,
    /**
     * Krava u periodu proizvodnje mleka nakon teljenja.
     */
    U_LAKTACIJI,
    /**
     * Krava u periodu zasusenja, kada se ne muze.
     */
    ZASUSENA,
    /**
     * Krava koja je izlucena iz proizvodnje i nad kojom nisu dozvoljene pojedine operacije.
     */
    IZLUCENA
}
