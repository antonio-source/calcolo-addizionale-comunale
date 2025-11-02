package it.pensioni.calcoloaddizionalecomunale.controllers;

import it.pensioni.calcoloaddizionalecomunale.dto.AddizionaleComunale;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.StatoComune;
import it.pensioni.calcoloaddizionalecomunale.repositories.DatiComuneRepository;
import it.pensioni.calcoloaddizionalecomunale.services.CalcolaAddizionaleComunaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "v1/api/addizionale-comunale")
public class CalcoloAddizionaleComunaleController {

    private final CalcolaAddizionaleComunaleService calcolaAddizionaleComunaleService;
    private final DatiComuneRepository datiComuneRepository;

    @Autowired
    public CalcoloAddizionaleComunaleController(CalcolaAddizionaleComunaleService calcolaAddizionaleComunaleService, DatiComuneRepository datiComuneRepository) {
        this.calcolaAddizionaleComunaleService = calcolaAddizionaleComunaleService;
        this.datiComuneRepository = datiComuneRepository;
    }

    @PostMapping("/calcola")
    public AddizionaleComunale calcolaAddizionaleComunale(int annoCalcolo, String codiceCatastale, double redditoImponibile) {
        return this.calcolaAddizionaleComunaleService.calcolaAddizionaleComunale(annoCalcolo, codiceCatastale, redditoImponibile);
    }

    @GetMapping("/carica-file")
    public String caricaFileAliquotePerAnno(int annoCalcolo) {
        calcolaAddizionaleComunaleService.caricaFileAliquotePerAnno(annoCalcolo);

        long implementatiCount = datiComuneRepository.countById_AnnoRiferimentoAndStato(annoCalcolo, StatoComune.IMPLEMENTATO);
        long scartatiCount = datiComuneRepository.countById_AnnoRiferimentoAndStato(annoCalcolo, StatoComune.SCARTATO);
        List<DatiComune> comuniScartati = datiComuneRepository.findById_AnnoRiferimentoAndStato(annoCalcolo, StatoComune.SCARTATO);

        StringBuilder output = new StringBuilder();
        output.append("NUMERO COMUNI IMPLEMENTATI: ").append(implementatiCount).append("\n");
        output.append("NUMERO COMUNI SCARTATI    : ").append(scartatiCount).append("\n");
        long numeroComuniTotali = implementatiCount + scartatiCount;
        output.append("NUMERO COMUNI TOTALI      : ").append(numeroComuniTotali).append("\n");
        output.append("-").append("\n");

        if (!comuniScartati.isEmpty()) {
            output.append("------------------------------ ELENCO COMUNI SCARTATI: ------------------------------\n");
            for (DatiComune comune : comuniScartati) {
                output.append("CODICE CATASTALE: ").append(comune.getId().getCodiceCatastale()).append(" - NOME: ").append(comune.getComune()).append("\n");
            }
            output.append("-------------------------------------------------------------------------------------\n");
            output.append("-").append("\n");
        }

        return output.toString();
    }
}
