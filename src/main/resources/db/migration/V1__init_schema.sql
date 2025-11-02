-- Script di inizializzazione dello schema per Flyway

-- Tabella per l'entità DatiComune
CREATE TABLE dati_comune (
    anno_riferimento INT NOT NULL,
    codice_catastale VARCHAR(255) NOT NULL,
    comune VARCHAR(255),
    multi_aliq BOOLEAN NOT NULL,
    esenzione_reddito DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (anno_riferimento, codice_catastale)
);

-- Tabella per l'entità AliquotaFascia
CREATE TABLE aliquota_fascia (
    anno_riferimento INT NOT NULL,
    codice_catastale VARCHAR(255) NOT NULL,
    limite_min DOUBLE PRECISION NOT NULL,
    limite_max DOUBLE PRECISION NOT NULL,
    aliquota DOUBLE PRECISION NOT NULL,
    dati_comune_anno_riferimento INT,
    dati_comune_codice_catastale VARCHAR(255),
    PRIMARY KEY (anno_riferimento, codice_catastale, limite_min, limite_max),
    FOREIGN KEY (dati_comune_anno_riferimento, dati_comune_codice_catastale) REFERENCES dati_comune(anno_riferimento, codice_catastale)
);

-- Tabella per l'entità FileAliquoteAddizionaliComunali
CREATE TABLE file_aliquote_addizionali_comunali (
    anno_riferimento INT NOT NULL,
    data_caricamento DATE,
    anomalia VARCHAR(255),
    PRIMARY KEY (anno_riferimento)
);

-- Tabella di join per la relazione ManyToMany tra FileAliquoteAddizionaliComunali e DatiComune (implementati)
CREATE TABLE file_comuni_implementati (
    file_anno_riferimento INT NOT NULL,
    comune_anno_riferimento INT NOT NULL,
    comune_codice_catastale VARCHAR(255) NOT NULL,
    PRIMARY KEY (file_anno_riferimento, comune_anno_riferimento, comune_codice_catastale),
    FOREIGN KEY (file_anno_riferimento) REFERENCES file_aliquote_addizionali_comunali(anno_riferimento),
    FOREIGN KEY (comune_anno_riferimento, comune_codice_catastale) REFERENCES dati_comune(anno_riferimento, codice_catastale)
);

-- Tabella di join per la relazione ManyToMany tra FileAliquoteAddizionaliComunali e DatiComune (scartati)
CREATE TABLE file_comuni_scartati (
    file_anno_riferimento INT NOT NULL,
    comune_anno_riferimento INT NOT NULL,
    comune_codice_catastale VARCHAR(255) NOT NULL,
    PRIMARY KEY (file_anno_riferimento, comune_anno_riferimento, comune_codice_catastale),
    FOREIGN KEY (file_anno_riferimento) REFERENCES file_aliquote_addizionali_comunali(anno_riferimento),
    FOREIGN KEY (comune_anno_riferimento, comune_codice_catastale) REFERENCES dati_comune(anno_riferimento, codice_catastale)
);
