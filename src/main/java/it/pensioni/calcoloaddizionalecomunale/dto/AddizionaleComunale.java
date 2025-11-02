package it.pensioni.calcoloaddizionalecomunale.dto;

import java.io.Serializable;
import java.util.Objects;

public class AddizionaleComunale implements Serializable {

    private static final long serialVersionUID = 1L;

    private int annoRiferimento;
    private String codiceCatastale;
    private DatiComune comune;
    private double redditoImponibile;
    private double importoAddizionaleComunale;
    private double importoAccontoAddizionaleComunale;
    private String anomalia;

    public AddizionaleComunale() {
    }

    public AddizionaleComunale(int annoRiferimento, DatiComune comune, double redditoImponibile) {
        this.annoRiferimento = annoRiferimento;
        this.comune = comune;
        if (comune != null) {
            this.codiceCatastale = comune.getId().getCodiceCatastale();
        }
        this.redditoImponibile = redditoImponibile;
    }

    public AddizionaleComunale(int annoRiferimento, DatiComune comune, String anomalia) {
        this.annoRiferimento = annoRiferimento;
        this.comune = comune;
        if (comune != null) {
            this.codiceCatastale = comune.getId().getCodiceCatastale();
        }
        this.anomalia = anomalia;
    }

    // Getters and Setters

    public int getAnnoRiferimento() {
        return annoRiferimento;
    }

    public void setAnnoRiferimento(int annoRiferimento) {
        this.annoRiferimento = annoRiferimento;
    }

    public String getCodiceCatastale() {
        return codiceCatastale;
    }

    public void setCodiceCatastale(String codiceCatastale) {
        this.codiceCatastale = codiceCatastale;
    }

    public DatiComune getComune() {
        return comune;
    }

    public void setComune(DatiComune comune) {
        this.comune = comune;
        if (comune != null) {
            this.codiceCatastale = comune.getId().getCodiceCatastale();
        }
    }

    public double getRedditoImponibile() {
        return redditoImponibile;
    }

    public void setRedditoImponibile(double redditoImponibile) {
        this.redditoImponibile = redditoImponibile;
    }

    public double getImportoAddizionaleComunale() {
        return importoAddizionaleComunale;
    }

    public void setImportoAddizionaleComunale(double importoAddizionaleComunale) {
        this.importoAddizionaleComunale = importoAddizionaleComunale;
    }

    public double getImportoAccontoAddizionaleComunale() {
        return importoAccontoAddizionaleComunale;
    }

    public void setImportoAccontoAddizionaleComunale(double importoAccontoAddizionaleComunale) {
        this.importoAccontoAddizionaleComunale = importoAccontoAddizionaleComunale;
    }

    public String getAnomalia() {
        return anomalia;
    }

    public void setAnomalia(String anomalia) {
        this.anomalia = anomalia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddizionaleComunale that = (AddizionaleComunale) o;
        return annoRiferimento == that.annoRiferimento &&
                Double.compare(that.redditoImponibile, redditoImponibile) == 0 &&
                Objects.equals(codiceCatastale, that.codiceCatastale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annoRiferimento, codiceCatastale, redditoImponibile);
    }

    @Override
    public String toString() {
        return "AddizionaleComunale{" +
                "annoRiferimento=" + annoRiferimento +
                ", codiceCatastale='" + codiceCatastale + '\'' +
                ", comune=" + (comune != null ? comune.getComune() : null) +
                ", redditoImponibile=" + redditoImponibile +
                ", importoAddizionaleComunale=" + importoAddizionaleComunale +
                ", importoAccontoAddizionaleComunale=" + importoAccontoAddizionaleComunale +
                ", anomalia='" + anomalia + '\'' +
                '}';
    }
}
