package it.pensioni.calcoloaddizionalecomunale.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import it.pensioni.calcoloaddizionalecomunale.dto.AddizionaleComunale;
import it.pensioni.calcoloaddizionalecomunale.dto.AliquotaFascia;
import it.pensioni.calcoloaddizionalecomunale.dto.AliquotaFasciaId;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComune;
import it.pensioni.calcoloaddizionalecomunale.dto.DatiComuneId;
import it.pensioni.calcoloaddizionalecomunale.dto.FileAliquoteAddizionaliComunali;
import it.pensioni.calcoloaddizionalecomunale.dto.StatoComune;
import it.pensioni.calcoloaddizionalecomunale.repositories.AliquotaFasciaRepository;
import it.pensioni.calcoloaddizionalecomunale.repositories.DatiComuneRepository;
import it.pensioni.calcoloaddizionalecomunale.repositories.FileAliquoteAddizionaliComunaliRepository;
import it.pensioni.calcoloaddizionalecomunale.utility.FasceUtility;

@Service
public class CalcolaAddizionaleComunaleService {

    private static final Logger log = LoggerFactory.getLogger(CalcolaAddizionaleComunaleService.class);

    private static final String DELIMITATORE = ";";
    private static final int INDICE_IMPORTO_ESENTE = 33;
    private static final int NUMERO_MINIMO_COLONNE = 34;

    @Autowired
    private FileAliquoteAddizionaliComunaliRepository fileAliquoteAddizionaliComunaliRepository;

    @Autowired
    private DatiComuneRepository datiComuneRepository;

    @Autowired
    private AliquotaFasciaRepository aliquotaFasciaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void cleanupDatabase(int annoRiferimento) {
        log.info("Cleaning up database tables before loading new data for year: " + annoRiferimento + "...");
        // Esegui le cancellazioni in ordine inverso rispetto alle dipendenze delle chiavi esterne
        jdbcTemplate.execute("DELETE FROM aliquota_fascia WHERE anno_riferimento = " + annoRiferimento);
        jdbcTemplate.execute("DELETE FROM dati_comune WHERE anno_riferimento = " + annoRiferimento);
        jdbcTemplate.execute("DELETE FROM file_aliquote_addizionali_comunali WHERE anno_riferimento = " + annoRiferimento);
        log.info("Database cleanup complete for year: " + annoRiferimento + ".");
    }
    
    private void cleanupDatabase(int annoRiferimento, String codiceCatastale) {
        log.info("Cleaning up database tables before loading new data for year: " + annoRiferimento + "...");
        // Esegui le cancellazioni in ordine inverso rispetto alle dipendenze delle chiavi esterne
        jdbcTemplate.execute("DELETE FROM aliquota_fascia WHERE anno_riferimento = " + annoRiferimento + " AND codice_catastale = '" + codiceCatastale + "'");
        jdbcTemplate.execute("DELETE FROM dati_comune WHERE anno_riferimento = " + annoRiferimento + " AND codice_catastale = '" + codiceCatastale + "'");
        log.info("Database cleanup complete for year: " + annoRiferimento + " and codiceCatastale: " + codiceCatastale + ".");
    }

    @javax.transaction.Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = RuntimeException.class)
    public List<DatiComune> caricaFileAliquotePerAnno(int annoCalcolo, String codiceCatastaleInput) {
    	if (ObjectUtils.isEmpty(codiceCatastaleInput)) {
    		cleanupDatabase(annoCalcolo);
    	} else {
    		cleanupDatabase(annoCalcolo, codiceCatastaleInput);
    	}

        List<DatiComune> listaComuni = new ArrayList<>();
        String NOME_FILE = "Add_comunale_irpef" + annoCalcolo + ".csv";
        ClassPathResource resource = new ClassPathResource("csv/aliquote-addizionali-comunali/" + NOME_FILE);

        fileAliquoteAddizionaliComunaliRepository.save(new FileAliquoteAddizionaliComunali(annoCalcolo));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line = br.readLine(); // Salta intestazione

            while ((line = br.readLine()) != null) {
                String cleanedLine = line.replaceAll("\\.", "").replaceAll(",", "\\.");
                String[] campi = cleanedLine.split(DELIMITATORE, -1);

                if (campi.length >= NUMERO_MINIMO_COLONNE) {
                	String codiceCatastale = campi[0];
                	if (!ObjectUtils.isEmpty(codiceCatastaleInput)
                			&& !codiceCatastale.equalsIgnoreCase(codiceCatastaleInput)) {
                		continue;
                	}
                    DatiComuneId comuneId = new DatiComuneId(annoCalcolo, codiceCatastale);
                    DatiComune comune = datiComuneRepository.findById(comuneId).orElse(new DatiComune(comuneId));
                    comune.setComune(campi[1]);
                    comune.setMultiAliq("SI".equalsIgnoreCase(campi[7]));

                    // Correzione per comuni che sono falsamente multi-aliquota
                    if (comune.isMultiAliq()) {
                        String descAliquota = campi[11].trim();
                        if (descAliquota.equalsIgnoreCase("Aliquota Unica")) {
                            comune.setMultiAliq(false);
                        }
                        if (descAliquota.contains("Esenzione")) {
                        	comune.setMultiAliq(false);
                        }
                    }

                    String aliquotaStr = campi[8].trim();
                    if (aliquotaStr.equalsIgnoreCase("0*")) {
                        comune.setStato(StatoComune.SCARTATO);
                        listaComuni.add(comune);
                        continue;
                    }
                    //double aliquotaCampo8 = Double.parseDouble(aliquotaStr);
                    
                    comune.setStato(StatoComune.IMPLEMENTATO);

                    try {
                        String esenzioneStr = campi[INDICE_IMPORTO_ESENTE].trim();
                        comune.setEsenzioneReddito(esenzioneStr.isEmpty() ? 0.0 : Double.parseDouble(esenzioneStr));
                    } catch (NumberFormatException e) {
                        comune.setEsenzioneReddito(0.0);
                    }

                    if (comune.isMultiAliq()) {
                        int startIndex = 10;
                        int endIndex   = 32;
                        double aliDouble = Double.valueOf(aliquotaStr);
                        if (aliDouble > 0.0) {
                            startIndex = 8;
                            endIndex   = 30;
                        }
                        for (int i = startIndex; i <= endIndex; i += 2) {
                            try {
                                if (i < campi.length && !campi[i].trim().isEmpty()) {
                                    AliquotaFasciaId afId = new AliquotaFasciaId();
                                    afId.setAnnoRiferimento(annoCalcolo);
                                    afId.setCodiceCatastale(comune.getId().getCodiceCatastale());

                                    String fasciaDesc = campi[i + 1].trim();
                                    
                                    //Controllo se la fascia è di tipo esenzione
                                    if (fasciaDesc.contains("Esenzione")) {
                                    	//System.err.println("[ATTENZIONE] " + comuneId.getAnnoRiferimento() + " - " + comuneId.getCodiceCatastale() + " FASCIA DI TIPO ESENZIONE LA SALTO => " + fasciaDesc);
                                    	continue;
                                    }
                                    try {
                                    	Double.parseDouble(fasciaDesc);
                                    	//System.err.println("[ATTENZIONE] " + comuneId.getAnnoRiferimento() + " - " + comuneId.getCodiceCatastale() + " FASCIA DI TIPO NUMERICO LA SALTO => " + fasciaDesc);
                                    	continue;
                                    } catch (Exception e) {
                                    	
                                    }
                                    boolean esito = updateFasciaLimits(afId, fasciaDesc, comune.getAliquote());
                                    if (!esito) {
                                    	System.err.println("[ATTENZIONE] " + comuneId.getAnnoRiferimento() + " - " + comuneId.getCodiceCatastale() + " PROBLEMI CON IL PARSING DELLE ALIQUOTE => " + fasciaDesc);
                                    }
                                    AliquotaFascia af = aliquotaFasciaRepository.findById(afId).orElse(new AliquotaFascia());
                                    af.setId(afId);
                                    af.setAliquota(Double.parseDouble(campi[i].trim()));

                                    if (!comune.getAliquote().contains(af)) {
                                        comune.getAliquote().add(af);
                                    }
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    } else {
                        try {
                            double aliquotaDouble = Double.parseDouble(aliquotaStr);
                            // Correzione per aliquota unica che è zero ma ha un valore nel campo successivo
                            if (aliquotaDouble == 0.0) {
                                String descAliquota = campi[11].trim();
                                if (descAliquota.equalsIgnoreCase("Aliquota Unica")) {
                                    aliquotaStr = campi[10].trim();
                                    aliquotaDouble = Double.parseDouble(aliquotaStr);
                                }
                            }
                            AliquotaFasciaId afId = new AliquotaFasciaId(annoCalcolo, comune.getId().getCodiceCatastale(), 0.0, Double.MAX_VALUE);
                            AliquotaFascia af = aliquotaFasciaRepository.findById(afId).orElse(new AliquotaFascia());
                            af.setId(afId);
                            af.setAliquota(aliquotaDouble);

                            if (!comune.getAliquote().contains(af)) {
                                comune.getAliquote().add(af);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    listaComuni.add(comune);
                }
            }
            System.out.println("Comuni size: " + listaComuni.size());
            this.datiComuneRepository.saveAll(listaComuni);
            return  listaComuni;
        } catch (IOException e) {
            log.error("IOException: {}", e.toString(), e);
            FileAliquoteAddizionaliComunali file = new FileAliquoteAddizionaliComunali(annoCalcolo);
            file.setAnomalia("Impossibile caricare il file " + NOME_FILE);
            fileAliquoteAddizionaliComunaliRepository.save(file);
            return listaComuni;
        }
    }

//    private boolean updateFasciaLimitsOLD(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
//    	if ((fasciaDesc.contains("Applicabile a Scaglione")
//    			|| fasciaDesc.contains("Applicabile a scaglione"))
//    			&& fasciaDesc.contains("euro")) {
//    		return updateFasciaLimitsWithParolaScaglioneEDaEuroAEuro(afId, fasciaDesc, existingFasce);
//    	}
//    	else if ((fasciaDesc.contains("Applicabile a Scaglione")
//    			|| fasciaDesc.contains("Applicabile a scaglione"))
//    			&& !fasciaDesc.contains("euro")) {
//    		return updateFasciaLimitsWithParolaScaglioneEDaA(afId, fasciaDesc, existingFasce);
//    	}
//    	
//    	Pattern pattern = null;
//    	Matcher matcher = null;
//    	pattern = Pattern.compile("fino a FINO A ([\\d\\.]+)");
//        matcher = pattern.matcher(fasciaDesc);
//        if (matcher.find()) {
//            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
//            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
//            afId.setLimiteMin(limiteMinimo);
//            return true;
//        } else {
//	    	pattern = Pattern.compile("fino a euro ([\\d\\.]+)");
//	        matcher = pattern.matcher(fasciaDesc);
//	        if (matcher.find()) {
//	            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
//	            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
//	            afId.setLimiteMin(limiteMinimo);
//	            return true;
//	        } else {
//	        	pattern = Pattern.compile("fino ad euro ([\\d\\.]+)");
//	            matcher = pattern.matcher(fasciaDesc);
//	            if (matcher.find()) {
//	                afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
//	                double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
//	                afId.setLimiteMin(limiteMinimo);
//	                return true;
//	            } else {
//		            pattern = Pattern.compile("da euro ([\\d\\.]+) fino a euro ([\\d\\.]+)");
//		            matcher = pattern.matcher(fasciaDesc);
//		            if (matcher.find()) {
//		                afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//		                afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
//		                return true;
//		            } else {
//		                pattern = Pattern.compile("oltre euro ([\\d\\.]+)");
//		                matcher = pattern.matcher(fasciaDesc);
//		                if (matcher.find()) {
//		                    afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//		                    afId.setLimiteMax(Double.MAX_VALUE);
//		                    return true;
//		                }
//		            }
//	            }
//	        }
//        }
//        return false;
//    }

    public AddizionaleComunale calcolaAddizionaleComunale(int annoCalcolo, String codiceCatastale, double redditoImponibile) {
        Optional<DatiComune> datiComuneOpt = datiComuneRepository.findById(new DatiComuneId(annoCalcolo, codiceCatastale));

        if (datiComuneOpt.isPresent()) {
            DatiComune comune = datiComuneOpt.get();
            if (comune.getStato() == StatoComune.SCARTATO) {
                return new AddizionaleComunale(annoCalcolo, comune, "Il comune indicato rientra fra quelli non implementati (ALIQUOTA = 0*)");
            }
            return calcolaAddizionale(annoCalcolo, comune, redditoImponibile);
        } else {
            throw new RuntimeException("Impossibile trovare le regole per il codice catastale: " + codiceCatastale + " nel file dell'anno: " + annoCalcolo);
        }
    }

    private AddizionaleComunale calcolaAddizionale(int annoCalcolo, DatiComune comune, double redditoImponibile) {
        AddizionaleComunale output = new AddizionaleComunale(annoCalcolo, comune, redditoImponibile);
        if (redditoImponibile <= comune.getEsenzioneReddito()) {
            log.info("----- COMUNE: {} - ESENZIONE TOTALE APPLICATA -----", comune.getId().getCodiceCatastale());
            return output;
        }

        double addizionaleTotale = 0.0;

        if (comune.isMultiAliq()) {
            log.info("----- COMUNE: {} - CALCOLO MULTIALQUOTA -----", comune.getId().getCodiceCatastale());
            log.info("    REDDITO IMPONIBILE: {}", redditoImponibile);
            List<AliquotaFascia> listaAliquote = comune.getAliquote().stream().
            		sorted((a1,a2) -> {
						             if (a1.getId().getLimiteMin() < a2.getId().getLimiteMin()) {
						            	 return -1;
						             }
						             else if (a1.getId().getLimiteMin() == a2.getId().getLimiteMin()) {
						            	 return 0;
						             }
						             else {
						            	 return 1;
						             }
						            })
            		.collect(Collectors.toList());
            
            for (AliquotaFascia af : listaAliquote) {
                if (redditoImponibile > af.getId().getLimiteMin() && redditoImponibile > af.getId().getLimiteMax()) {
                	double baseImponibileFascia = af.getId().getLimiteMax();
                    if (af.getId().getLimiteMin() == 0.0) {
                        baseImponibileFascia -= comune.getEsenzioneReddito();
                    }
                    if (baseImponibileFascia > 0) {
                        double quotaFascia = (baseImponibileFascia * af.getAliquota() / 100);
                        addizionaleTotale += quotaFascia;
                        log.info("    FASCIA MIN: {} - MAX: {} - ALIQUOTA: {} - BASE IMPONIBILE: {} - QUOTA FASCIA: {}", af.getId().getLimiteMin(), af.getId().getLimiteMax(), af.getAliquota(), baseImponibileFascia, quotaFascia);
                    }
                } else if (redditoImponibile > af.getId().getLimiteMin() && redditoImponibile <= af.getId().getLimiteMax()) {
                    double baseImponibileFascia = redditoImponibile - af.getId().getLimiteMin();
                    if (af.getId().getLimiteMin() == 0.0) {
                        baseImponibileFascia -= comune.getEsenzioneReddito();
                    }
                    if (baseImponibileFascia > 0) {
                        double quotaFascia = (baseImponibileFascia * af.getAliquota() / 100);
                        addizionaleTotale += quotaFascia;
                        log.info("    FASCIA MIN: {} - MAX: {} - ALIQUOTA: {} - BASE IMPONIBILE: {} - QUOTA FASCIA: {}", af.getId().getLimiteMin(), af.getId().getLimiteMax(), af.getAliquota(), baseImponibileFascia, quotaFascia);
                    }
                }
            }
        } else {
            log.info("----- COMUNE: {} - CALCOLO ALIQUOTA UNICA -----", comune.getId().getCodiceCatastale());
            log.info("    REDDITO IMPONIBILE: {}", redditoImponibile);
            if (redditoImponibile > comune.getEsenzioneReddito()) {
                if (!comune.getAliquote().isEmpty()) {
                    addizionaleTotale = redditoImponibile * comune.getAliquote().get(0).getAliquota() / 100;
                    log.info("    ALIQUOTA: {} - IMPOSTA CALCOLATA: {}", comune.getAliquote().get(0).getAliquota(), addizionaleTotale);
                } else {
                    addizionaleTotale = 0;
                    log.info("    ATTENZIONE COMUNE CONFIGURATO NON CORRETTAMENTE: " + comune);
                }
            }
        }
        output.setImportoAddizionaleComunale(addizionaleTotale);
        output.setImportoAccontoAddizionaleComunale(addizionaleTotale / 100 * 30);
        return output;
    }

    public String generaReportCsv(int annoCalcolo, double redditoImponibile) throws IOException {
        List<DatiComune> comuni = datiComuneRepository.findById_AnnoRiferimentoAndStato(annoCalcolo, StatoComune.IMPLEMENTATO)
        		                                      .stream()
        		                                      .sorted((d1, d2) -> {
        		                                    			  return d1.getId().getCodiceCatastale().compareTo(d2.getId().getCodiceCatastale());
        		                                      }
        		                                     ).collect(Collectors.toList());

        StringWriter stringWriter = new StringWriter();
        
        CSVFormat format = CSVFormat.EXCEL.builder()
            .setHeader("Anno", "Codice Catastale", "Comune", "Reddito Imponibile", "Addizionale Calcolata", "Acconto Addizionale")
            .setDelimiter(';')
            .build();

        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, format)) {
            for (DatiComune comune : comuni) {
            	if (comune.getId().getCodiceCatastale().equals("A034")) {
            		System.err.println("DEBUG");
            	}
                AddizionaleComunale risultato = calcolaAddizionale(annoCalcolo, comune, redditoImponibile);
                csvPrinter.printRecord(
                    risultato.getAnnoRiferimento(),
                    risultato.getCodiceCatastale(),
                    (risultato.getComune() != null) ? risultato.getComune().getComune() : "",
                    formattaImporti(risultato.getRedditoImponibile()),
                    formattaImporti(risultato.getImportoAddizionaleComunale()),
                    formattaImporti(risultato.getImportoAccontoAddizionaleComunale())
                );
            }
        }

        return stringWriter.toString();
    }
    
    private String formattaImporti(double importo) {
    	NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALIAN);
    	numberFormat.setMaximumFractionDigits(2);
    	numberFormat.setMinimumFractionDigits(2);
    	String formattato = numberFormat.format(importo);
    	String formattatoSenzaMigliaia = formattato.replaceAll("\\.", "");
    	return formattatoSenzaMigliaia;
    }
    
    
//    private boolean updateFasciaLimitsWithParolaScaglioneEDaA(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
//        Pattern pattern = Pattern.compile(" da ([\\d\\.]+) a ([\\d\\.]+)");
//        Matcher matcher = pattern.matcher(fasciaDesc);
//        if (matcher.find()) {
//            afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//            afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
//            return true;
//        } else {
//        	pattern = Pattern.compile(" oltre ([\\d\\.]+)");
//            matcher = pattern.matcher(fasciaDesc);
//            if (matcher.find()) {
//                afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//                afId.setLimiteMax(Double.MAX_VALUE);
//                return true;
//            }
//        }
//        return false;
//    }
    
    
//    private boolean updateFasciaLimitsWithParolaScaglioneEDaEuroAEuro(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
//        Pattern pattern = Pattern.compile(" da euro ([\\d\\.]+) a euro ([\\d\\.]+)");
//        Matcher matcher = pattern.matcher(fasciaDesc);
//        if (matcher.find()) {
//            afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//            afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
//            return true;
//        } else {
//        	pattern = Pattern.compile(" da euro ([\\d\\.]+) fino a euro ([\\d\\.]+)");
//            matcher = pattern.matcher(fasciaDesc);
//            if (matcher.find()) {
//                afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//                afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
//                return true;
//            } else {
//            	  pattern = Pattern.compile(" da euro ([\\d\\.]+) fino ad euro ([\\d\\.]+)");
//                matcher = pattern.matcher(fasciaDesc);
//                if (matcher.find()) {
//                    afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//                    afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
//                    return true;
//                } else {
//                	pattern = Pattern.compile(" oltre euro ([\\d\\.]+)");
//                	matcher = pattern.matcher(fasciaDesc);
//                	if (matcher.find()) {
//                		afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
//                		afId.setLimiteMax(Double.MAX_VALUE);
//                		return true;
//                	} else {
//                		pattern = Pattern.compile(" di reddito fino a euro ([\\d\\.]+)");
//                		matcher = pattern.matcher(fasciaDesc);
//                		if (matcher.find()) {
//                			afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
//                			afId.setLimiteMax(0.0);
//                			return true;
//                		}
//                	}
//                }
//            }
//        } 
//        return false;
//    }
    
    /**
     * Valorizza l'aliquota finale che contiene le parole ' oltre euro [valore]'
     * @param  afId
     * @param  fasciaDesc
     * @param  existingFasce
     * @return aggiornata
     */
    private boolean updateFasciaLimits001(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
    	Pattern pattern = null;
    	Matcher matcher = null;
    	pattern = Pattern.compile(" oltre euro ([\\d\\.]+)");
    	matcher = pattern.matcher(fasciaDesc);
    	if (matcher.find()) {
    		afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
    		afId.setLimiteMax(Double.MAX_VALUE);
    		return true;
    	} 
    	return false;
    }
    
    
    
    /**
     * Valorizza l'aliquota finale che contiene le parole ' fino a euro [valore]'
     * @param  afId
     * @param  fasciaDesc
     * @param  existingFasce
     * @return aggiornata
     */
    private boolean updateFasciaLimits002(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
    	Pattern pattern = null;
    	Matcher matcher = null;
    	pattern = Pattern.compile(" fino a euro a ([\\d\\.]+)");
        matcher = pattern.matcher(fasciaDesc);
        if (matcher.find()) {
            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
            afId.setLimiteMin(limiteMinimo);
            return true;
        }
    	return false;
    }
    private boolean updateFasciaLimits003(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
    	Pattern pattern = null;
    	Matcher matcher = null;
    	pattern = Pattern.compile("fino a euro  ([\\d\\.]+)");
        matcher = pattern.matcher(fasciaDesc);
        if (matcher.find()) {
            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
            afId.setLimiteMin(limiteMinimo);
            return true;
        }
    	return false;
    }
    
    /**
     * Valorizza l'aliquota finale che contiene le parole ' fino a euro [valore]'
     * @param  afId
     * @param  fasciaDesc
     * @param  existingFasce
     * @return aggiornata
     */
    private boolean updateFasciaLimits004(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
    	Pattern pattern = null;
    	Matcher matcher = null;
    	pattern = Pattern.compile("fino a euro ([\\d\\.]+)");
        matcher = pattern.matcher(fasciaDesc);
        if (matcher.find()) {
            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
            afId.setLimiteMin(limiteMinimo);
            return true;
        }
    	return false;
    }
    
    private boolean updateFasciaLimits005(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
    	Pattern pattern = null;
    	Matcher matcher = null;
    	pattern = Pattern.compile("fino ad euro ([\\d\\.]+)");
        matcher = pattern.matcher(fasciaDesc);
        if (matcher.find()) {
            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
            afId.setLimiteMin(limiteMinimo);
            return true;
        }
    	return false;
    }
    
    private boolean updateFasciaLimits(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
//    	if (fasciaDesc.equals("Applicabile a  scaglione di reddito da euro 28000.01 fino a euro 50000.00")) {
//    		System.out.println("DEBUG");
//    	}
    	//                       Applicabile a Scaglione da 0 a 15000.00
    	if (fasciaDesc.contains("Applicabile a scaglione di reddito ") 
    			&& fasciaDesc.contains(" oltre euro ")) {
    		return updateFasciaLimits001(afId, fasciaDesc, existingFasce);
    	}
    	else if (fasciaDesc.contains("Applicabile a scaglione di reddito ") 
    			&& fasciaDesc.contains(" fino a euro a ")) {
    		return updateFasciaLimits002(afId, fasciaDesc, existingFasce);
    	}
    	else if (fasciaDesc.contains("Applicabile a scaglione di reddito ") 
    			&& fasciaDesc.contains(" fino a euro  ")) {
    		return updateFasciaLimits003(afId, fasciaDesc, existingFasce);
    	}
    	else if (fasciaDesc.contains("Applicabile a scaglione di reddito ") 
    			&& fasciaDesc.contains(" fino a euro ")) {
    		return updateFasciaLimits004(afId, fasciaDesc, existingFasce);
    	}
    	else if (fasciaDesc.contains("Applicabile a  scaglione di reddito ") 
    			&& fasciaDesc.contains(" fino a euro ")) {
    		return updateFasciaLimits004(afId, fasciaDesc, existingFasce);
    	}
    	else if (fasciaDesc.contains("Applicabile a scaglione di reddito ") 
    			&& fasciaDesc.contains(" fino ad euro ")) {
    		return updateFasciaLimits005(afId, fasciaDesc, existingFasce);
    	}
    	//---------------------------------------------------------------
    	else if (FasceUtility.isDescrizioneFra0e15000(fasciaDesc)) {
    		afId.setLimiteMin(0.0);
    		afId.setLimiteMax(15000.0);
    		return true;
    	}
    	else if (FasceUtility.isDescrizioneFra15000e28000(fasciaDesc)) {
    		afId.setLimiteMin(15000.01);
    		afId.setLimiteMax(28000.00);
    		return true;
    	}
    	else if (FasceUtility.isDescrizioneFra28000e50000(fasciaDesc)) {
    		afId.setLimiteMin(28000.01);
    		afId.setLimiteMax(50000.0);
    		return true;
    	}
    	else if (FasceUtility.isDescrizioneOltre50000(fasciaDesc)) {
    		afId.setLimiteMin(50000.01);
    		afId.setLimiteMax(Double.MAX_VALUE);
    		return true;
    	}
    	//---------------------------------------------------------------

    	return false;
    }
}
