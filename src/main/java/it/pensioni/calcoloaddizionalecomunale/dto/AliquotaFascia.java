package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class AliquotaFascia {

    @EmbeddedId
    private AliquotaFasciaId id;

    private double aliquota;  // Valore dell'aliquota (es. 0.0076 per 0,76%)

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "annoRiferimento", referencedColumnName = "annoRiferimento", insertable = false, updatable = false),
        @JoinColumn(name = "codiceCatastale", referencedColumnName = "codiceCatastale", insertable = false, updatable = false)
    })
    private DatiComune datiComune;

    public AliquotaFascia() {
        super();
    }

    // Getters and Setters

    public AliquotaFasciaId getId() {
        return id;
    }

    public void setId(AliquotaFasciaId id) {
        this.id = id;
    }

    public double getAliquota() {
        return aliquota;
    }

    public void setAliquota(double aliquota) {
        this.aliquota = aliquota;
    }

    public DatiComune getDatiComune() {
        return datiComune;
    }

    public void setDatiComune(DatiComune datiComune) {
        this.datiComune = datiComune;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AliquotaFascia that = (AliquotaFascia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
