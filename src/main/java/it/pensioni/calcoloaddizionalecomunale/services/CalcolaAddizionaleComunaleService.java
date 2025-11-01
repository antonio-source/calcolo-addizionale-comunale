package it.pensioni.calcoloaddizionalecomunale.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.pensioni.calcoloaddizionalecomunale.dto.AddizionaleComunale;
import it.pensioni.calcoloaddizionalecomunale.dto.AliquotaFascia;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.FileAliquoteAddizionaliComunali;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;



@Service
public class CalcolaAddizionaleComunaleService {
	
	private static Logger log = LoggerFactory.getLogger(CalcolaAddizionaleComunaleService.class);

	private static final String DELIMITATORE = ";";
    
    // Nuovo indice corretto per IMPORTO_ESENTE (il 34° campo, indice 33)
    private static final int INDICE_IMPORTO_ESENTE = 33; 
    private static final int NUMERO_MINIMO_COLONNE = 34;
    
	public FileAliquoteAddizionaliComunali caricaFileAliquotePerAnno(int annoCalcolo) {
		String NOME_FILE = "Add_comunale_irpef" + annoCalcolo + ".csv";
		//String NOME_FILE = "Add_comunale_irpef" + annoCalcolo + "_TEST.csv";
		
		ClassPathResource resource = new ClassPathResource("csv/aliquote-addizionali-comunali/" + NOME_FILE);
		List<DatiComune> listaDatiComuniImplementati = new ArrayList<>();
		List<DatiComune> listaDatiComuniScartati     = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            // Salta l'intestazione
            String line = br.readLine(); 
            
            while ((line = br.readLine()) != null) {
                // Sostituisce la virgola usata come separatore decimale con il punto
                String cleanedLine = line.replaceAll("\\.", "").replaceAll(",", "\\."); 
                // Divisione dei campi utilizzando il delimitatore
                String[] campi = cleanedLine.split(DELIMITATORE, -1); // Uso -1 per includere le trailing stringhe vuote
                
                if (campi.length >= NUMERO_MINIMO_COLONNE) {
                    DatiComune comune = new DatiComune();
                    comune.setCodiceCatastale(campi[0]);
                    comune.setComune(campi[1]);
                    comune.setMultiAliq( "SI".equalsIgnoreCase(campi[7]));
                    
                    String aliquotaStr    = campi[8].trim();
                	if (aliquotaStr.equalsIgnoreCase("0*")) {
                		listaDatiComuniScartati.add(comune);
                		continue;
                	}
                	
                    // Lettura dell'esenzione dall'indice 33 (IMPORTO_ESENTE)
                    try {
                        String esenzioneStr = campi[INDICE_IMPORTO_ESENTE].trim();
                        if (esenzioneStr.isEmpty()) {
                             comune.setEsenzioneReddito(0.0);
                        } else {
                             // Il campo IMPORTO_ESENTE non ha separatori migliaia, quindi è una semplice conversione
                             comune.setEsenzioneReddito(Double.parseDouble(esenzioneStr));
                        }

                    } catch (NumberFormatException e) {
                        // Se il campo non è numerico o non è presente, l'esenzione è 0
                    	comune.setEsenzioneReddito(0.0); 
                    }
                    
                    // Parsing delle aliquote/fasce
                    if (comune.isMultiAliq()) {
                        // Logica basata sulle fasce (ALIQUOTA_2, FASCIA_2, ecc.)
                        // Le fasce partono dal campo 10 (ALIQUOTA_2) fino al campo 32 (FASCIA_12)
                        
                        for (int i = 10; i <= 32; i += 2) {
                            try {
                                if (i < campi.length && !campi[i].trim().isEmpty()) {
                                    AliquotaFascia af = new AliquotaFascia();
                                    // Aliquota (es. 0.76 diventa 0.0076)
                                    af.setAliquota(Double.parseDouble(campi[i].trim())); 
                                    
                                    // Parsing della fascia di reddito
                                    if (i + 1 < campi.length) {
                                        String fasciaDesc = campi[i+1].trim();
                                        
                                        // Espressione regolare per estrarre i limiti di reddito
                                        Pattern pattern = Pattern.compile("fino a euro ([\\d\\.]+)");
                                        Matcher matcher = pattern.matcher(fasciaDesc);
                                        
                                        if (matcher.find()) { // Fascia "fino a"
                                            // Rimuove il punto come separatore migliaia, poi converte
                                        	String limiteMaxStr = matcher.group(1);
                                            af.setLimiteMax(Double.parseDouble(limiteMaxStr)); 
                                            // Il limite minimo sarà il limite massimo della fascia precedente o 0
                                            double limiteMinimo = 0.0;
                                            if (comune.getAliquote().isEmpty()) {
                                            	limiteMinimo = 0.0;
                                            } else {
                                            	AliquotaFascia fasciaPrec = comune.getAliquote().get(comune.getAliquote().size() - 1);
                                            	limiteMinimo = fasciaPrec.getLimiteMax() + 0.01;
                                            }
                                            af.setLimiteMin(limiteMinimo);
                                        } else {
                                            pattern = Pattern.compile("da euro ([\\d\\.]+) fino a euro ([\\d\\.]+)");
                                            matcher = pattern.matcher(fasciaDesc);
                                            if (matcher.find()) { // Fascia "da... fino a..."
                                            	String limiteMinStr = matcher.group(1);
                                            	String limiteMaxStr = matcher.group(2);
                                                af.setLimiteMin(Double.parseDouble(limiteMinStr));
                                                af.setLimiteMax(Double.parseDouble(limiteMaxStr));
                                            } else {
                                                pattern = Pattern.compile("oltre euro ([\\d\\.]+)");
                                                matcher = pattern.matcher(fasciaDesc);
                                                if (matcher.find()) { // Fascia "oltre"
                                                	String limiteMinStr = matcher.group(1);
                                                    af.setLimiteMin(Double.parseDouble(limiteMinStr));
                                                    af.setLimiteMax(Double.MAX_VALUE); // Limite massimo infinito
                                                } else {
                                                    // Fallback: ignora se la descrizione non è riconosciuta
                                                    continue; 
                                                }
                                            }
                                        }
                                        comune.getAliquote().add(af);
                                    }
                                }
                            } catch (NumberFormatException ignored) {
                                // Ignora se i campi non sono validi 
                            }
                        }
                    } else {
                        // Logica per aliquota unica (campo 8 - ALIQUOTA)
                        try {
                        	double aliquotaDouble = Double.parseDouble(aliquotaStr);
                            if (aliquotaDouble == 0.0) {
                            	String fascia_2 = campi[11].trim();
                            	if (fascia_2.equalsIgnoreCase("Aliquota Unica")) {
                            		aliquotaStr    = campi[10].trim();
                                    aliquotaDouble = Double.parseDouble(aliquotaStr);
                            	}
                            }
                            AliquotaFascia af = new AliquotaFascia();
                            // Rimuove l'asterisco se presente (indica che l'aliquota è effettivamente applicata se si supera l'esenzione totale)
                            aliquotaStr = aliquotaStr.replace("*", "");
                            
                            af.setAliquota (aliquotaDouble);
                            af.setLimiteMin(0.0);
                            af.setLimiteMax(Double.MAX_VALUE);
                            comune.getAliquote().add(af);
                        } catch (NumberFormatException ignored) {
                            // Ignora se l'aliquota non è valida
                        }
                    }
                    listaDatiComuniImplementati.add(comune);
                }
            }
            return new FileAliquoteAddizionaliComunali(annoCalcolo, listaDatiComuniImplementati, listaDatiComuniScartati);
        } catch (IOException e) {
            log.error("IOException: {}", e.toString(), e);
            return new FileAliquoteAddizionaliComunali(annoCalcolo, "Impossibile caricare il file " + NOME_FILE + " contenente le aliquote per l'anno " + annoCalcolo);
        }
	}
	
	/**
     * Metodo per il calcolo dell'addizionale IRPEF comunale.
     * @param comune I dati del comune (aliquote, fasce, esenzione)
     * @param redditoImponibile Il reddito su cui calcolare l'imposta
     * @return L'importo totale dell'addizionale comunale
     */
    private AddizionaleComunale calcolaAddizionale(int annoCalcolo, DatiComune comune, double redditoImponibile) {
    	AddizionaleComunale output = new AddizionaleComunale(annoCalcolo, comune.getCodiceCatastale(), comune.getComune(), redditoImponibile);
        // 1. Verifica Esenzione Totale (esenzione per reddito fino a un certo importo)
        if (redditoImponibile <= comune.getEsenzioneReddito()) {
            return output; // Esenzione totale
        }

        double addizionaleTotale = 0.0;

        // 2. Calcolo per Scaglioni (Multi-aliquota)
        if (comune.isMultiAliq()) {
        	log.info("----- COMUNE: {} - CALCOLO MULTIALQUOTA -----", comune.getCodiceCatastale());
        	log.info("    REDDITO IMPONIBILE: {}", redditoImponibile);
            // Cicla su tutte le fasce disponibili
            for (AliquotaFascia af : comune.getAliquote()) {
                // Se il reddito imponibile supera il limite inferiore della fascia
                if (redditoImponibile > af.getLimiteMin() && redditoImponibile > af.getLimiteMax()) {
                	double baseImponibileFascia = af.getLimiteMax() - (af.getLimiteMin() == 0.0 ? comune.getEsenzioneReddito() : 0.0);
                    double quotaFascia = (baseImponibileFascia * af.getAliquota() / 100);
                    addizionaleTotale += quotaFascia;
                    log.info("    FASCIA MIN: {} - MAX: {} - ALIQUOTA: {} - BASE IMPONIBILE: {} - QUOTA FASCIA: {}", af.getLimiteMin(), af.getLimiteMax(), af.getAliquota(), baseImponibileFascia, quotaFascia);
                }
                else if (redditoImponibile > af.getLimiteMin() && redditoImponibile <= af.getLimiteMax()) {
                	double baseImponibileFascia = redditoImponibile - af.getLimiteMin();
                	double quotaFascia = (baseImponibileFascia * af.getAliquota() / 100);
                    addizionaleTotale += quotaFascia;
                    log.info("    FASCIA MIN: {} - MAX: {} - ALIQUOTA: {} - BASE IMPONIBILE: {} - QUOTA FASCIA: {}", af.getLimiteMin(), af.getLimiteMax(), af.getAliquota(), baseImponibileFascia, quotaFascia);
                }
            }
        } 
        // 3. Calcolo per Aliquota Unica (con eventuale Esenzione per Reddito)
        else {
            // Se l'aliquota è unica, l'imposta si calcola sul (Reddito Imponibile - Esenzione)
        	if (redditoImponibile <= comune.getEsenzioneReddito()) {
        		addizionaleTotale = 0.0;
        	} else {
        		addizionaleTotale = redditoImponibile * comune.getAliquote().get(0).getAliquota() / 100;
        	}
        }
        output.setImportoAddizionaleComunale(addizionaleTotale);
        output.setImportoAccontoAddizionaleComunale(addizionaleTotale / 100 * 30);
        return output;
    }
    
    /**
     * 
     * @param  annoCalcolo
     * @param  codiceCatastale
     * @param  redditoImponibile
     * @return AddizionaleComunale
     */
    public AddizionaleComunale calcolaAddizionaleComunale(int annoCalcolo, String codiceCatastale, double redditoImponibile) {
    	FileAliquoteAddizionaliComunali fileAliquoteAddizionali = caricaFileAliquotePerAnno(annoCalcolo);
    	if (!ObjectUtils.isEmpty(fileAliquoteAddizionali.getAnomalia())) {
    		return new AddizionaleComunale(annoCalcolo, null, null, fileAliquoteAddizionali.getAnomalia());
    	}
    	
    	if (fileAliquoteAddizionali.getListaDatiComuniImplementati().isEmpty()) {
    		throw new RuntimeException("Impossibile caricare il file delle aliquote per l'anno: " + annoCalcolo);
    	} else {
    		log.info("NUMERO COMUNI IMPLEMENTATI: {}", fileAliquoteAddizionali.getListaDatiComuniImplementati().size());
    		log.info("NUMERO COMUNI SCARTATI    : {}", fileAliquoteAddizionali.getListaDatiComuniScartati().size());
    		log.info("NUMERO COMUNI TOTALI      : {}", fileAliquoteAddizionali.getListaDatiComuniImplementati().size() + fileAliquoteAddizionali.getListaDatiComuniScartati().size());
    		log.info("-");
    	}
    	
//    	if (!fileAliquoteAddizionali.getListaDatiComuniScartati().isEmpty()) {
//    		log.info("------------------------------ ELENCO COMUNI SCARTATI: ------------------------------");
//    		for (DatiComune comune : fileAliquoteAddizionali.getListaDatiComuniScartati()) {
//    			log.info("CODICE CATASTALE: {} - NOME: {}", comune.getCodiceCatastale(), comune.getComune());
//    		}
//    		log.info("-------------------------------------------------------------------------------------");
//    		log.info("-");
//    	}
    	
    	DatiComune datiComune = fileAliquoteAddizionali.getListaDatiComuniImplementati().stream()
    			                           												.filter(dc -> dc.getCodiceCatastale().equalsIgnoreCase(codiceCatastale))
    			                           												.findFirst()
    			                           												.orElse(null);
    	
    	if (datiComune == null) {
    		DatiComune datiComuneScartato = fileAliquoteAddizionali.getListaDatiComuniScartati().stream()
						.filter(dc -> dc.getCodiceCatastale().equalsIgnoreCase(codiceCatastale))
						.findFirst()
						.orElse(null);
    		if (datiComuneScartato == null) {
    			throw new RuntimeException("Impossibile trovare le regole per il codice catastale: " + codiceCatastale + " nel file dell'anno: " + annoCalcolo);
    		} else {
    			return new AddizionaleComunale(annoCalcolo, datiComuneScartato.getCodiceCatastale(), datiComuneScartato.getComune(), "Il comune indicato rientra fra quelli non implementati (ALITUOTA = 0*)");
    		}
    	}
    	return calcolaAddizionale(annoCalcolo, datiComune, redditoImponibile);
    }
}
