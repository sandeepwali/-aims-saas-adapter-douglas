package com.solumesl.aims.saas.dou.dto;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement(name="Artikel")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ArticleDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6913852684399476377L;
	@XmlElement(name = "Artikelnummer", required = true)
	private String artikelNummer;
	@XmlElement(name = "Artikeltext", required = true)
	private String artikelText;
	@XmlElement(name = "Basisinhalt", required = true)
	private String basisInhalt;
	@XmlElement(name = "Preisgegenueberstellung", required = true)
	private String preisgegenueberstellung;
	@XmlElement(name = "Warengruppe", required = true)
	private String warengruppe;
	@XmlElement(name = "Lieferantenartikelnummer", required = true)
	private String lieferantenartikelnummer;
	@XmlElement(name = "Lieferantenname", required = true)
	private String lieferantenname;
	@XmlElement(name = "Linientext", required = true)
	private String linientext;
	@XmlElement(name = "Depottext", required = true)
	private String depottext;
	@XmlElement(name = "Packungsinhalt", required = true)
	private String packungsinhalt;
	@XmlElement(name = "Linie", required = true)
	private String linie;
	@XmlElement(name = "Depot", required = true)
	private String depot;
	@XmlElement(name = "UVP", required = true)
	private String uvp;
	@XmlElement(name = "Loeschkennzeichen", required = true)
	private String loeschkennzeichen;
	
	
	@XmlElementWrapper(name = "Eanliste")
	@XmlElements({
	    @XmlElement(name = "EAN", type = String.class, required = true)
	})
	private List<String> eanliste;







}
