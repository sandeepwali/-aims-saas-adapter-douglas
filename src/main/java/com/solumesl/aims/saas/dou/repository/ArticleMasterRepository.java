package com.solumesl.aims.saas.dou.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.solumesl.aims.saas.dou.dto.ArticlePricing;
import com.solumesl.aims.saas.dou.entity.ArticleMaster;
import com.solumesl.aims.saas.dou.entity.ArticleMasterPK;

public interface ArticleMasterRepository extends PagingAndSortingRepository<ArticleMaster,ArticleMasterPK>{
	
	boolean existsByHashedCode(String hashCode);
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE ArticleMaster c SET c.markForTransfer = false WHERE 1 = 1")
    int updateMarkForTransfer();
	
	@Query(
			value = "SELECT articlemaster.artikelnummer as artikelNummer, articlemaster.data as articleData, pricingdata.data as pricingData, pricingdata.storecode as storeCode FROM articlemaster "
					+ "INNER JOIN pricingdata ON articlemaster.artikelnummer=pricingdata.artikelnummer "
					+ "where pricingdata.storecode =:storeCode and articlemaster.markfortransfer=true order by artikelNummer",
			countQuery = "SELECT count(*) FROM articlemaster "
					+ "INNER JOIN pricingdata ON articlemaster.artikelnummer=pricingdata.artikelnummer "
					+ "where pricingdata.storecode =:storeCode and articlemaster.markfortransfer=true",
			nativeQuery = true)
	Page<ArticlePricing> findMarkForTransferInArticles(@Param("storeCode") String storeCode, Pageable pageable);
	

} 