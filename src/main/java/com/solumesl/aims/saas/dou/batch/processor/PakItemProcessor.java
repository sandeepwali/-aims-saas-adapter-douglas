package com.solumesl.aims.saas.dou.batch.processor;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solumesl.aims.saas.dou.dto.PricingDataDTO;
import com.solumesl.aims.saas.dou.entity.PricingData;
public class PakItemProcessor implements ItemProcessor<PricingDataDTO, PricingData>{
	ObjectMapper mapper = new ObjectMapper();
	@Value("#{jobParameters['storeCode']}")
	private String storeCode;
	@Override
	public PricingData process(PricingDataDTO item) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		String json = mapper.writeValueAsString(item);
		byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
		String encoded = Base64.getEncoder().encodeToString(hash);
		return new PricingData(item,encoded, storeCode );
	}
}
