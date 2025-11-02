package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DatiComuneId implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int annoRiferimento;
    private String codiceCatastale;

    public DatiComuneId() {
    }

    public DatiComuneId(int annoRiferimento, String codiceCatastale) {
        this.annoRiferimento = annoRiferimento;
        this.codiceCatastale = codiceCatastale;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatiComuneId that = (DatiComuneId) o;
        return annoRiferimento == that.annoRiferimento &&
                Objects.equals(codiceCatastale, that.codiceCatastale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annoRiferimento, codiceCatastale);
    }
}
