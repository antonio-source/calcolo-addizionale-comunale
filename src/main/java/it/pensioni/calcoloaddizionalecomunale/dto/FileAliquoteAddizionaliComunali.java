package it.pensioni.calcoloaddizionalecomunale.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileAliquoteAddizionaliComunali implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int              annoRiferimento;

	private List<DatiComune> listaDatiComuniImplementati = new ArrayList<>();
	
	private List<DatiComune> listaDatiComuniScartati     = new ArrayList<>();

	private String           anomalia;
	
	public FileAliquoteAddizionaliComunali() {
		super();
	}
	
	public FileAliquoteAddizionaliComunali(int annoRiferimento, List<DatiComune> listaDatiComuniImplementati, List<DatiComune> listaDatiComuniScartati) {
		super();
		this.annoRiferimento = annoRiferimento;
		setListaDatiComuniImplementati(listaDatiComuniImplementati);
		setListaDatiComuniScartati(listaDatiComuniScartati);
	}
	
	public FileAliquoteAddizionaliComunali(int annoRiferimento, String anomalia) {
		super();
		this.annoRiferimento = annoRiferimento;
		this.anomalia        = anomalia;
	}
	
	public List<DatiComune> getListaDatiComuniImplementati() {
		return listaDatiComuniImplementati;
	}

	public void setListaDatiComuniImplementati(List<DatiComune> listaDatiComuniImplementati) {
		this.listaDatiComuniImplementati = listaDatiComuniImplementati != null ? listaDatiComuniImplementati : new ArrayList<>();
	}

	public List<DatiComune> getListaDatiComuniScartati() {
		return listaDatiComuniScartati;
	}

	public void setListaDatiComuniScartati(List<DatiComune> listaDatiComuniScartati) {
		this.listaDatiComuniScartati = listaDatiComuniScartati != null ? listaDatiComuniScartati : new ArrayList<>();
	}

	public int getAnnoRiferimento() {
		return annoRiferimento;
	}

	public void setAnnoRiferimento(int annoRiferimento) {
		this.annoRiferimento = annoRiferimento;
	}

	public String getAnomalia() {
		return anomalia;
	}

	public void setAnomalia(String anomalia) {
		this.anomalia = anomalia;
	}
}
