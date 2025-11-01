package it.pensioni.calcoloaddizionalecomunale.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class DatiComune implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String codiceCatastale;
    private String comune;
    private boolean multiAliq;
    private double esenzioneReddito; // Importo massimo del reddito imponibile esente
    private List<AliquotaFascia> aliquote; // Lista di aliquote e fasce di reddito

    public DatiComune() {
        aliquote = new ArrayList<>();
    }

	public String getCodiceCatastale() {
		return codiceCatastale;
	}

	public void setCodiceCatastale(String codiceCatastale) {
		this.codiceCatastale = codiceCatastale;
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
}