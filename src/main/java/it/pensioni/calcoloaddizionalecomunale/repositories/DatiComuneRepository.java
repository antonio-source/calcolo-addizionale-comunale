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

    List<DatiComune> findById_AnnoRiferimentoAndStato(int annoRiferimento, StatoComune stato);

}
