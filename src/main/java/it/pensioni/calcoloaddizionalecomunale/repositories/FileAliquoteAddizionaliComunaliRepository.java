package it.pensioni.calcoloaddizionalecomunale.repositories;

import it.pensioni.calcoloaddizionalecomunale.dto.FileAliquoteAddizionaliComunali;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileAliquoteAddizionaliComunaliRepository extends PagingAndSortingRepository<FileAliquoteAddizionaliComunali, Integer> {
}
