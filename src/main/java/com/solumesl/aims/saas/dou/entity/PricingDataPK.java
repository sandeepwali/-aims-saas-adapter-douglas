package com.solumesl.aims.saas.dou.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class PricingDataPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String artikelNummer;
	private String storeCode;


	public   PricingDataPK() {

	}
	public   PricingDataPK(PricingData pricingData) {
		this.artikelNummer = pricingData.getArtikelNummer();
		this.storeCode = pricingData.getStoreCode();
	}
}
