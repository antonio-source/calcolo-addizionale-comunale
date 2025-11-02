package it.pensioni.calcoloaddizionalecomunale.repositories;

import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComuneId;
import it.pensioni.calcoloaddizionalecomunale.dto.StatoComune;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatiComuneRepository extends PagingAndSortingRepository<DatiComune, DatiComuneId> {

    long countById_AnnoRiferimentoAndStato(int annoRiferimento, StatoComune stato);

    @Query("SELECT DISTINCT dc FROM DatiComune dc LEFT JOIN FETCH dc.aliquote WHERE dc.id.annoRiferimento = :annoRiferimento AND dc.stato = :stato")
    List<DatiComune> findById_AnnoRiferimentoAndStatoWithAliquote(@Param("annoRiferimento") int annoRiferimento, @Param("stato") StatoComune stato);

    List<DatiComune> findById_AnnoRiferimentoAndStato(int annoRiferimento, StatoComune stato);

}
