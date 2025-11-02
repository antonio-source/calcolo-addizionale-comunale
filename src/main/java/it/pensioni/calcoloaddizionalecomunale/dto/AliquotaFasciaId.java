package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AliquotaFasciaId implements Serializable {

    private int annoRiferimento;
    private String codiceCatastale;
    private double limiteMin;
    private double limiteMax;

    public AliquotaFasciaId() {
    }

    public AliquotaFasciaId(int annoRiferimento, String codiceCatastale, double limiteMin, double limiteMax) {
        this.annoRiferimento = annoRiferimento;
        this.codiceCatastale = codiceCatastale;
        this.limiteMin = limiteMin;
        this.limiteMax = limiteMax;
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

    public double getLimiteMin() {
        return limiteMin;
    }

    public void setLimiteMin(double limiteMin) {
        this.limiteMin = limiteMin;
    }

    public double getLimiteMax() {
        return limiteMax;
    }

    public void setLimiteMax(double limiteMax) {
        this.limiteMax = limiteMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AliquotaFasciaId that = (AliquotaFasciaId) o;
        return annoRiferimento == that.annoRiferimento &&
                Double.compare(that.limiteMin, limiteMin) == 0 &&
                Double.compare(that.limiteMax, limiteMax) == 0 &&
                Objects.equals(codiceCatastale, that.codiceCatastale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annoRiferimento, codiceCatastale, limiteMin, limiteMax);
    }
}
