package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
public class DatiComune implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DatiComuneId id;

    private String comune;

    private boolean multiAliq;

    private double esenzioneReddito; // Importo massimo del reddito imponibile esente

    @OneToMany(mappedBy = "datiComune", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AliquotaFascia> aliquote = new ArrayList<>();

    public DatiComune() {
    }

    // Getters and Setters

    public DatiComuneId getId() {
        return id;
    }

    public void setId(DatiComuneId id) {
        this.id = id;
    }

	public String getComune() {
		return comune;
	}

	public void setComune(String comune) {
		this.comune = comune;
	}

	public boolean isMultiAliq() {
		return multiAliq;
	}

	public void setMultiAliq(boolean multiAliq) {
		this.multiAliq = multiAliq;
	}

	public double getEsenzioneReddito() {
		return esenzioneReddito;
	}

	public void setEsenzioneReddito(double esenzioneReddito) {
		this.esenzioneReddito = esenzioneReddito;
	}

	public List<AliquotaFascia> getAliquote() {
		return aliquote;
	}

	public void setAliquote(List<AliquotaFascia> aliquote) {
		this.aliquote = aliquote;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatiComune that = (DatiComune) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}