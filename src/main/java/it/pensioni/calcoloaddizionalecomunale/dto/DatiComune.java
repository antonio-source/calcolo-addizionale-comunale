package it.pensioni.calcoloaddizionalecomunale.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
public class DatiComune implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @EmbeddedId
    private DatiComuneId id;

    private String comune;

    @Enumerated(EnumType.STRING)
    private StatoComune stato;

    private boolean multiAliq;

    private double esenzioneReddito; // Importo massimo del reddito imponibile esente

    @OneToMany(mappedBy = "datiComune", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AliquotaFascia> aliquote = new ArrayList<>();

    public DatiComune() {
    }

    public DatiComune(DatiComuneId id) {
        this.id = id;
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

    public StatoComune getStato() {
        return stato;
    }

    public void setStato(StatoComune stato) {
        this.stato = stato;
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