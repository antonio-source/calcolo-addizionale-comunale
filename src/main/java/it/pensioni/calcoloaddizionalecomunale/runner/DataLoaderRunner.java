package it.pensioni.calcoloaddizionalecomunale.runner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import it.pensioni.calcoloaddizionalecomunale.services.CalcolaAddizionaleComunaleService;

@Component
public class DataLoaderRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderRunner.class);
    private final CalcolaAddizionaleComunaleService service;
    
    public DataLoaderRunner(CalcolaAddizionaleComunaleService service) {
        this.service = service;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
    public void run(String... args) throws Exception {
        log.info("### STARTING DATA PRE-LOADING ###");

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:csv/aliquote-addizionali-comunali/Add_comunale_irpef*.csv");

        if (resources.length == 0) {
            log.warn("No CSV files found in 'resources/csv/aliquote-addizionali-comunali/'. Skipping data loading.");
            return;
        }

        Pattern pattern = Pattern.compile("Add_comunale_irpef(\\d{4})\\.csv");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null) {

                if (filename.toUpperCase().endsWith("_TEST.CSV")) {
                    log.info("Skipping test file: {}", filename);
                    continue; // Salta il file di test
                }

                Matcher matcher = pattern.matcher(filename);
                if (matcher.find()) {
                    try {
                        int year = Integer.parseInt(matcher.group(1));
                        log.info("-> Loading data for year: {}", year);
                        service.caricaFileAliquotePerAnno(year, null);
                        log.info("-> Successfully loaded data for year: {}", year);
                    } catch (NumberFormatException e) {
                        log.error("Could not parse year from filename: {}", filename, e);
                    } catch (Exception e) {
                        log.error("An error occurred while loading data for file: {}", filename, e);
                    }
                }
            }
        }
        log.info("### DATA PRE-LOADING FINISHED ###");
    }


}
