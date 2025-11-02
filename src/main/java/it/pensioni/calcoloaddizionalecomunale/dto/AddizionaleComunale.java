package it.pensioni.calcoloaddizionalecomunale.dto;

import java.io.Serializable;
import java.util.Objects;

public class AddizionaleComunale implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int    annoRiferimento;
	
	private String codiceCatastale;
	
	private String comune;
	
	private double redditoImponibile;

	private double importoAddizionaleComunale;
	
	private double importoAccontoAddizionaleComunale;
	
	private String anomalia;
	
	public AddizionaleComunale() {
		super();
	}
	
	public AddizionaleComunale(int annoRiferimento, String codiceCatastale, String comune, double redditoImponibile) {
		super();
		this.annoRiferimento   = annoRiferimento;
		this.codiceCatastale   = codiceCatastale;
		this.comune            = comune;
		this.redditoImponibile = redditoImponibile;
	}
	
	public AddizionaleComunale(int annoRiferimento, String codiceCatastale, String comune, String anomalia) {
		super();
		this.annoRiferimento   = annoRiferimento;
		this.codiceCatastale   = codiceCatastale;
		this.comune            = comune;
		this.anomalia          = anomalia;
	}

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

	public String getComune() {
		return comune;
	}

	public void setComune(String comune) {
		this.comune = comune;
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
	public int hashCode() {
		return Objects.hash(annoRiferimento, codiceCatastale, redditoImponibile);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddizionaleComunale other = (AddizionaleComunale) obj;
		return annoRiferimento == other.annoRiferimento && Objects.equals(codiceCatastale, other.codiceCatastale)
				&& Double.doubleToLongBits(redditoImponibile) == Double.doubleToLongBits(other.redditoImponibile);
	}

	@Override
	public String toString() {
		return "AddizionaleComunale [annoRiferimento=" + annoRiferimento + ", redditoImponibile=" + redditoImponibile
				+ ", codiceCatastale=" + codiceCatastale + ", importoAddizionaleComunale=" + importoAddizionaleComunale
				+ ", importoAccontoAddizionaleComunale=" + importoAccontoAddizionaleComunale + ", anomalia=" + anomalia
				+ "]";
	}
}
