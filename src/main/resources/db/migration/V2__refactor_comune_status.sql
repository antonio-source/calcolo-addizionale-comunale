-- Rimuove le tabelle di join obsolete
DROP TABLE IF EXISTS file_comuni_implementati;
DROP TABLE IF EXISTS file_comuni_scartati;

-- Aggiunge la colonna 'stato' alla tabella dati_comune
ALTER TABLE dati_comune ADD COLUMN stato VARCHAR(255);
