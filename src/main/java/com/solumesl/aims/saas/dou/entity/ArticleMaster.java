package com.solumesl.aims.saas.dou.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.solumesl.aims.saas.adapter.entity.job.audit.Auditable;
import com.solumesl.aims.saas.dou.dto.ArticleDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@EntityListeners(AuditingEntityListener.class)
@IdClass(ArticleMasterPK.class)
@Table(name = "articlemaster",indexes = {
		@Index(columnList = "artikelnummer"),
		@Index(name = "hashcode_index", columnList = "hashedcode"),
		@Index(name = "mulitIndex1", columnList = "artikelnummer, hashedcode"),
		@Index(name = "uniqueMulitIndex", columnList = "artikelnummer, hashedcode", unique = true)
})
/*
 * @TypeDefs({
 * 
 * @TypeDef(name = "json", typeClass = JsonBinaryType.class) })
 */
public class ArticleMaster extends Auditable{

	@Id
	@Column(name = "artikelnummer")
	private String artikelNummer;
	/*
	 * @Column(columnDefinition = "json")
	 * 
	 * @Type(type = "json")
	 */
	private ArticleDTO data;
	@Id
	@Column(name = "hashedcode")
	private String hashedCode;
	@Column(name = "markfortransfer")
	private boolean markForTransfer;

	public ArticleMaster() {
		super();
	}

	public ArticleMaster(ArticleDTO data, String hash) {
		this.artikelNummer = data.getArtikelNummer();
		this.data = data;
		this.hashedCode = hash;
	}
}
