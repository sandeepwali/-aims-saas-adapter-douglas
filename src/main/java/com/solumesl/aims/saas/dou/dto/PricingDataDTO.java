package com.solumesl.aims.saas.dou.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PricingDataDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3456237851131088073L;
	private String update;
	private String artikelNr;
	private String preisAktuelle;
	private String preisListe;
	private String grundpreiskennzeichen;
	private String grundpreis;
	private String vergleichsinhalt;
	private String mengeneinheit;
	private String Hintergrundfarbe;







}
