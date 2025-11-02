package it.pensioni.calcoloaddizionalecomunale.controllers;

import it.pensioni.calcoloaddizionalecomunale.dto.AddizionaleComunale;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.FileAliquoteAddizionaliComunali;
import it.pensioni.calcoloaddizionalecomunale.services.CalcolaAddizionaleComunaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "v1/api/addizionale-comunale")
public class CalcoloAddizionaleComunaleController {

	//private static Logger log = LoggerFactory.getLogger(CalcoloAddizionaleComunaleController.class);
	
	@Autowired
	private CalcolaAddizionaleComunaleService calcolaAddizionaleComunaleService;
	
	@PostMapping("/calcola")
	public AddizionaleComunale calcolaAddizionaleComunale(int annoCalcolo, String codiceCatastale, double redditoImponibile) {
		AddizionaleComunale addizionaleComunale = this.calcolaAddizionaleComunaleService.calcolaAddizionaleComunale(annoCalcolo, codiceCatastale, redditoImponibile);
		return addizionaleComunale;
	}
	
	@GetMapping("/carica-file")
	public String caricaFileAliquotePerAnno(int annoCalcolo) {
		FileAliquoteAddizionaliComunali fileAliquoteAddizionali = this.calcolaAddizionaleComunaleService.caricaFileAliquotePerAnno(annoCalcolo);
		if (ObjectUtils.isEmpty(fileAliquoteAddizionali.getAnomalia())) {
			StringBuilder output = new StringBuilder();
			output.append("NUMERO COMUNI IMPLEMENTATI: " + fileAliquoteAddizionali.getListaDatiComuniImplementati().size()).append("\n");
			output.append("NUMERO COMUNI SCARTATI    : " + fileAliquoteAddizionali.getListaDatiComuniScartati().size()).append("\n");
			int numeroComuniTotali = fileAliquoteAddizionali.getListaDatiComuniImplementati().size() + fileAliquoteAddizionali.getListaDatiComuniScartati().size();
			output.append("NUMERO COMUNI TOTALI      : " ).append(numeroComuniTotali).append("\n");
			output.append("-").append("\n");
			if (!fileAliquoteAddizionali.getListaDatiComuniScartati().isEmpty()) {
				output.append("------------------------------ ELENCO COMUNI SCARTATI: ------------------------------").append("\n");
	    		for (DatiComune comune : fileAliquoteAddizionali.getListaDatiComuniScartati()) {
	    			output.append("CODICE CATASTALE: ").append(comune.getId().getCodiceCatastale()).append(" - NOME: ").append(comune.getComune()).append("\n");
	    		}
	    		output.append("-------------------------------------------------------------------------------------").append("\n");
	    		output.append("-").append("\n");
	    	}
			return output.toString();
		} else {
			return fileAliquoteAddizionali.getAnomalia();
		}
	}
}
