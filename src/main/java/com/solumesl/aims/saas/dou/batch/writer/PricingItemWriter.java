package com.solumesl.aims.saas.dou.batch.writer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.solumesl.aims.saas.dou.entity.PricingData;
import com.solumesl.aims.saas.dou.repository.PricingDataRepository;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class PricingItemWriter implements ItemWriter<PricingData> {
	@Autowired
	private PricingDataRepository pricingDataRepository;
	@Override
	public void write(List<? extends PricingData> items) throws Exception {

		List<? extends PricingData> deltaPricing = items.parallelStream()
				.filter(a-> !pricingDataRepository.existsByHashedCodeAndStoreCode(a.getHashedCode(),a.getStoreCode())).map(c->{
					c.setMarkForTransfer(true);
					return c;
				})
				.collect(Collectors.toList());
		log.debug(" PricingItemWriter delta Size {}",deltaPricing.size());
		 
		 try {
			pricingDataRepository.saveAll(deltaPricing);
		} catch (Exception e) {
		log.error("{}",e.getMessage());
		}

	}

	

}