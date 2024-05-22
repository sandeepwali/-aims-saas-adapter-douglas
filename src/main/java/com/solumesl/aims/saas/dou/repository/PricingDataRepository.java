package com.solumesl.aims.saas.dou.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solumesl.aims.saas.dou.dto.ArticlePricing;
import com.solumesl.aims.saas.dou.entity.PricingData;

public interface PricingDataRepository extends JpaRepository<PricingData,String>{
	
	boolean existsByHashedCodeAndStoreCode(String hashCode, String storeCode);
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE PricingData c SET c.markForTransfer = false WHERE c.storeCode = :storeCode")
    int updateMarkForTransfer(@Param("storeCode") String storeCode);
	
	@Query(
			value = "SELECT articlemaster.artikelnummer as artikelNummer, articlemaster.data as articleData, pricingdata.data as pricingData, pricingdata.storecode as storeCode FROM articlemaster "
					+ "INNER JOIN pricingdata ON articlemaster.artikelnummer=pricingdata.artikelnummer "
					+ "where pricingdata.storecode =:storeCode and pricingdata.markfortransfer=true order by artikelNummer",
			countQuery = "SELECT count(*) FROM articlemaster "
					+ "INNER JOIN pricingdata ON articlemaster.artikelnummer=pricingdata.artikelnummer "
					+ "where pricingdata.storecode =:storeCode and pricingdata.markfortransfer=true",
			nativeQuery = true)
	Page<ArticlePricing> findMarkForTransferInPricing(@Param("storeCode") String storeCode, Pageable pageable);

} 