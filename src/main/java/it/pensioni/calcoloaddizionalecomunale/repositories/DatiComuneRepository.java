package it.pensioni.calcoloaddizionalecomunale.repositories;

import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComuneId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatiComuneRepository extends PagingAndSortingRepository<DatiComune, DatiComuneId> {

    @Query("SELECT dc FROM FileAliquoteAddizionaliComunali f JOIN f.listaDatiComuniImplementati dc WHERE f.annoRiferimento = :anno AND dc.id.codiceCatastale = :codiceCatastale")
    Optional<DatiComune> findImplementatoByAnnoAndCodiceCatastale(@Param("anno") int anno, @Param("codiceCatastale") String codiceCatastale);

    @Query("SELECT dc FROM FileAliquoteAddizionaliComunali f JOIN f.listaDatiComuniScartati dc WHERE f.annoRiferimento = :anno AND dc.id.codiceCatastale = :codiceCatastale")
    Optional<DatiComune> findScartatoByAnnoAndCodiceCatastale(@Param("anno") int anno, @Param("codiceCatastale") String codiceCatastale);

}
