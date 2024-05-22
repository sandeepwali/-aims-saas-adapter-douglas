package com.solumesl.aims.saas.dou.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.solumesl.aims.saas.adapter.entity.job.audit.Auditable;
import com.solumesl.aims.saas.dou.dto.PricingDataDTO;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "pricingdata",indexes = {
		  @Index(columnList = "artikelnummer"),
		  @Index(name = "hashcode_index1", columnList = "hashedcode"), @Index(name = "storecode_index1", columnList = "storecode"),
		  @Index(name = "mulitIndex11", columnList = "storecode,artikelnummer, hashedcode"),
		  @Index(name = "uniqueMulitIndex11", columnList = "storecode,artikelnummer, hashedcode", unique = true)
		})
@IdClass(PricingDataPK.class)
/*
 * @TypeDefs({
 * 
 * @TypeDef(name = "json", typeClass = JsonBinaryType.class) })
 */
public class PricingData extends Auditable{

	@Id
	@Column(name = "artikelnummer")
	private String artikelNummer;
	@Id
	@Column(name = "storecode")
	private String storeCode;
	/*
	 * @Column(columnDefinition = "json")
	 * 
	 * @Type(type = "json")
	 */
	private PricingDataDTO data;
	@Id
	@Column(name = "hashedcode")
	private String hashedCode;
	@Column(name = "markfortransfer")
	private boolean markForTransfer;
	
	public PricingData() {
		super();
	}
	
	public PricingData(PricingDataDTO data, String hash, String storeCode) {
		this.artikelNummer = data.getArtikelNr();
		this.data = data;
		this.storeCode = storeCode;
		this.hashedCode = hash;
	}
}
