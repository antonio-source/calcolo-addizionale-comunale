package it.pensioni.calcoloaddizionalecomunale.repositories;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComuneId;
import it.pensioni.calcoloaddizionalecomunale.dto.StatoComune;

@Repository
public interface DatiComuneRepository extends PagingAndSortingRepository<DatiComune, DatiComuneId> {

    long countById_AnnoRiferimentoAndStato(int annoRiferimento, StatoComune stato);

    List<DatiComune> findById_AnnoRiferimentoAndStato(int annoRiferimento, StatoComune stato);

}
