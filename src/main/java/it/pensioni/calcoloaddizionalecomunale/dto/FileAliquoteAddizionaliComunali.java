package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class FileAliquoteAddizionaliComunali implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int annoRiferimento;

    private LocalDate dataCaricamento = LocalDate.now();

    private String anomalia;

    public FileAliquoteAddizionaliComunali() {
    }

    public FileAliquoteAddizionaliComunali(int annoRiferimento) {
        this.annoRiferimento = annoRiferimento;
    }

    public FileAliquoteAddizionaliComunali(int annoRiferimento, String anomalia) {
        this.annoRiferimento = annoRiferimento;
        this.anomalia = anomalia;
    }

    // Getters and Setters

    public int getAnnoRiferimento() {
        return annoRiferimento;
    }

    public void setAnnoRiferimento(int annoRiferimento) {
        this.annoRiferimento = annoRiferimento;
    }

    public LocalDate getDataCaricamento() {
        return dataCaricamento;
    }

    public void setDataCaricamento(LocalDate dataCaricamento) {
        this.dataCaricamento = dataCaricamento;
    }

    public String getAnomalia() {
        return anomalia;
    }

    public void setAnomalia(String anomalia) {
        this.anomalia = anomalia;
    }
}
