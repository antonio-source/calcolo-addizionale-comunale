package it.pensioni.calcoloaddizionalecomunale.dto;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileAliquoteAddizionaliComunali implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @Id
	private int              annoRiferimento;

    private LocalDate        dataCaricamento  = LocalDate.now();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "file_comuni_implementati",
        joinColumns = @JoinColumn(name = "file_anno_riferimento", referencedColumnName = "annoRiferimento"),
        inverseJoinColumns = {
            @JoinColumn(name = "comune_anno_riferimento", referencedColumnName = "annoRiferimento"),
            @JoinColumn(name = "comune_codice_catastale", referencedColumnName = "codiceCatastale")
        }
    )
	private List<DatiComune> listaDatiComuniImplementati = new ArrayList<>();
	
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "file_comuni_scartati",
        joinColumns = @JoinColumn(name = "file_anno_riferimento", referencedColumnName = "annoRiferimento"),
        inverseJoinColumns = {
            @JoinColumn(name = "comune_anno_riferimento", referencedColumnName = "annoRiferimento"),
            @JoinColumn(name = "comune_codice_catastale", referencedColumnName = "codiceCatastale")
        }
    )
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

    public LocalDate getDataCaricamento() {
        return dataCaricamento;
    }

    public void setDataCaricamento(LocalDate dataCaricamento) {
        this.dataCaricamento = dataCaricamento;
    }
}
