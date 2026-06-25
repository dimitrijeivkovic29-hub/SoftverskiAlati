package rs.fon.bg.ac.rs.farma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.fon.bg.ac.rs.farma.domain.Krava;
import rs.fon.bg.ac.rs.farma.domain.StatusKrave;

import java.util.List;

public interface KravaRepository extends JpaRepository<Krava, Long> {
    boolean existsByBrojMarkice(String brojMarkice);
    boolean existsByBrojMarkiceAndIdNot(String brojMarkice, Long id);
    List<Krava> findByStatus(StatusKrave status);
    List<Krava> findByFarmaId(Long farmaId);
    long countByFarmaId(Long farmaId);
    long countByFarmaIdAndStatus(Long farmaId, StatusKrave status);

    @Query("""
        select k from Krava k
        where (:brojMarkice is null or lower(k.brojMarkice) like lower(concat('%', :brojMarkice, '%')))
          and (:status is null or k.status = :status)
        order by k.brojMarkice
        """)
    List<Krava> pretrazi(@Param("brojMarkice") String brojMarkice, @Param("status") StatusKrave status);
}
