package it.pensioni.calcoloaddizionalecomunale.repositories;

import it.pensioni.calcoloaddizionalecomunale.dto.AliquotaFascia;
import it.pensioni.calcoloaddizionalecomunale.dto.AliquotaFasciaId;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AliquotaFasciaRepository extends PagingAndSortingRepository<AliquotaFascia, AliquotaFasciaId> {
}
