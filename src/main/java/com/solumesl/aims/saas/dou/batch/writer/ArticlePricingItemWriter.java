package com.solumesl.aims.saas.dou.batch.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.solumesl.aims.saas.dou.dto.DouglasSaasData;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ArticlePricingItemWriter implements ItemWriter<DouglasSaasData> {
 
	@Override
	public void write(List<? extends DouglasSaasData> items) throws Exception {
		
		log.info("Transformed size {}",items.size());
		 
	}

	

}