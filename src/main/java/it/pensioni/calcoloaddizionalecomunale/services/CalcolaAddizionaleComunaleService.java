package it.pensioni.calcoloaddizionalecomunale.services;

import it.pensioni.calcoloaddizionalecomunale.dto.*;
import it.pensioni.calcoloaddizionalecomunale.repositories.AliquotaFasciaRepository;
import it.pensioni.calcoloaddizionalecomunale.repositories.DatiComuneRepository;
import it.pensioni.calcoloaddizionalecomunale.repositories.FileAliquoteAddizionaliComunaliRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Transactional
    public void caricaFileAliquotePerAnno(int annoCalcolo) {
        String NOME_FILE = "Add_comunale_irpef" + annoCalcolo + ".csv";
        ClassPathResource resource = new ClassPathResource("csv/aliquote-addizionali-comunali/" + NOME_FILE);

        fileAliquoteAddizionaliComunaliRepository.save(new FileAliquoteAddizionaliComunali(annoCalcolo));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line = br.readLine(); // Salta intestazione

            while ((line = br.readLine()) != null) {
                String cleanedLine = line.replaceAll("\\.", "").replaceAll(",", "\\.");
                String[] campi = cleanedLine.split(DELIMITATORE, -1);

                if (campi.length >= NUMERO_MINIMO_COLONNE) {
                    DatiComuneId comuneId = new DatiComuneId(annoCalcolo, campi[0]);
                    DatiComune comune = datiComuneRepository.findById(comuneId).orElse(new DatiComune());
                    comune.setId(comuneId);
                    comune.setComune(campi[1]);
                    comune.setMultiAliq("SI".equalsIgnoreCase(campi[7]));

                    // Correzione per comuni che sono falsamente multi-aliquota
                    if (comune.isMultiAliq()) {
                        String descAliquota = campi[11].trim();
                        if (descAliquota.equalsIgnoreCase("Aliquota Unica")) {
                            comune.setMultiAliq(false);
                        }
                    }

                    String aliquotaStr = campi[8].trim();
                    if (aliquotaStr.equalsIgnoreCase("0*")) {
                        comune.setStato(StatoComune.SCARTATO);
                        datiComuneRepository.save(comune);
                        continue;
                    }

                    comune.setStato(StatoComune.IMPLEMENTATO);

                    try {
                        String esenzioneStr = campi[INDICE_IMPORTO_ESENTE].trim();
                        comune.setEsenzioneReddito(esenzioneStr.isEmpty() ? 0.0 : Double.parseDouble(esenzioneStr));
                    } catch (NumberFormatException e) {
                        comune.setEsenzioneReddito(0.0);
                    }

                    if (comune.isMultiAliq()) {
                        for (int i = 10; i <= 32; i += 2) {
                            try {
                                if (i < campi.length && !campi[i].trim().isEmpty()) {
                                    AliquotaFasciaId afId = new AliquotaFasciaId();
                                    afId.setAnnoRiferimento(annoCalcolo);
                                    afId.setCodiceCatastale(comune.getId().getCodiceCatastale());

                                    String fasciaDesc = campi[i + 1].trim();
                                    updateFasciaLimits(afId, fasciaDesc, comune.getAliquote());

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
                            double aliquotaDouble = Double.parseDouble(aliquotaStr.replace("*", ""));
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
                    datiComuneRepository.save(comune);
                }
            }
        } catch (IOException e) {
            log.error("IOException: {}", e.toString(), e);
            FileAliquoteAddizionaliComunali file = new FileAliquoteAddizionaliComunali(annoCalcolo);
            file.setAnomalia("Impossibile caricare il file " + NOME_FILE);
            fileAliquoteAddizionaliComunaliRepository.save(file);
        }
    }

    private void updateFasciaLimits(AliquotaFasciaId afId, String fasciaDesc, List<AliquotaFascia> existingFasce) {
        Pattern pattern = Pattern.compile("fino a euro ([\\d\\.]+)");
        Matcher matcher = pattern.matcher(fasciaDesc);
        if (matcher.find()) {
            afId.setLimiteMax(Double.parseDouble(matcher.group(1)));
            double limiteMinimo = existingFasce.isEmpty() ? 0.0 : existingFasce.get(existingFasce.size() - 1).getId().getLimiteMax() + 0.01;
            afId.setLimiteMin(limiteMinimo);
        } else {
            pattern = Pattern.compile("da euro ([\\d\\.]+) fino a euro ([\\d\\.]+)");
            matcher = pattern.matcher(fasciaDesc);
            if (matcher.find()) {
                afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
                afId.setLimiteMax(Double.parseDouble(matcher.group(2)));
            } else {
                pattern = Pattern.compile("oltre euro ([\\d\\.]+)");
                matcher = pattern.matcher(fasciaDesc);
                if (matcher.find()) {
                    afId.setLimiteMin(Double.parseDouble(matcher.group(1)));
                    afId.setLimiteMax(Double.MAX_VALUE);
                }
            }
        }
    }

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
            for (AliquotaFascia af : comune.getAliquote()) {
                if (redditoImponibile > af.getId().getLimiteMin()) {
                    double baseImponibileFascia = Math.min(redditoImponibile, af.getId().getLimiteMax()) - af.getId().getLimiteMin();
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
                addizionaleTotale = redditoImponibile * comune.getAliquote().get(0).getAliquota() / 100;
                log.info("    ALIQUOTA: {} - IMPOSTA CALCOLATA: {}", comune.getAliquote().get(0).getAliquota(), addizionaleTotale);
            }
        }
        output.setImportoAddizionaleComunale(addizionaleTotale);
        output.setImportoAccontoAddizionaleComunale(addizionaleTotale / 100 * 30);
        return output;
    }
}
