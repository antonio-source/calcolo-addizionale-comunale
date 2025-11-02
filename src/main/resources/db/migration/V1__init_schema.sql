-- Script di inizializzazione dello schema per Flyway
DROP TABLE IF EXISTS file_aliquote_addizionali_comunali;
DROP TABLE IF EXISTS aliquota_fascia;
DROP TABLE IF EXISTS dati_comune;


-- Tabella per l'entità DatiComune
CREATE TABLE dati_comune (
    anno_riferimento INT NOT NULL,
    codice_catastale VARCHAR(255) NOT NULL,
    comune VARCHAR(255),
    multi_aliq BOOLEAN NOT NULL,
    esenzione_reddito DOUBLE PRECISION NOT NULL,
    stato VARCHAR(255),
    PRIMARY KEY (anno_riferimento, codice_catastale)
);


-- Tabella per l'entità AliquotaFascia
CREATE TABLE aliquota_fascia (
    anno_riferimento INT NOT NULL,
    codice_catastale VARCHAR(255) NOT NULL,
    limite_min DOUBLE PRECISION NOT NULL,
    limite_max DOUBLE PRECISION NOT NULL,
    aliquota DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (anno_riferimento, codice_catastale, limite_min, limite_max),
    FOREIGN KEY (anno_riferimento, codice_catastale) REFERENCES dati_comune(anno_riferimento, codice_catastale)
);



-- Tabella per l'entità FileAliquoteAddizionaliComunali
CREATE TABLE file_aliquote_addizionali_comunali (
    anno_riferimento INT NOT NULL,
    data_caricamento DATE,
    anomalia VARCHAR(255),
    PRIMARY KEY (anno_riferimento)
);
