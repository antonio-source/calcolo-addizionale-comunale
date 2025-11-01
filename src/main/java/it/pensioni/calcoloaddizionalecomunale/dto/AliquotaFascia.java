package it.pensioni.calcoloaddizionalecomunale.dto;

public class AliquotaFascia {

	private double aliquota;  // Valore dell'aliquota (es. 0.0076 per 0,76%)
	private double limiteMin; // Limite inferiore della fascia
	private double limiteMax; // Limite superiore della fascia
	
	public AliquotaFascia() {
		super();
	}

	public double getAliquota() {
		return aliquota;
	}

	public void setAliquota(double aliquota) {
		this.aliquota = aliquota;
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
}
