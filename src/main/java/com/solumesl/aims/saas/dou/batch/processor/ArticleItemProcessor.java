package com.solumesl.aims.saas.dou.batch.processor;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.batch.item.ItemProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solumesl.aims.saas.dou.dto.ArticleDTO;
import com.solumesl.aims.saas.dou.entity.ArticleMaster;

public class ArticleItemProcessor implements ItemProcessor<ArticleDTO, ArticleMaster>{
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public ArticleMaster process(ArticleDTO item) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		String json = mapper.writeValueAsString(item);
		byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
		String encoded = Base64.getEncoder().encodeToString(hash);
		return new ArticleMaster(item,encoded);
	}
}
