package com.solumesl.aims.saas.dou.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class ArticleMasterPK implements Serializable {
 
	private static final long serialVersionUID = 1L;
	
	private String artikelNummer;
 
 
	 public   ArticleMasterPK() {
		 
	 }
	 public   ArticleMasterPK(ArticleMaster articleMaster) {
		 this.artikelNummer = articleMaster.getArtikelNummer();
	 }
}
