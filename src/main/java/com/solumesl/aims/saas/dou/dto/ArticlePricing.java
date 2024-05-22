package com.solumesl.aims.saas.dou.dto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

public interface ArticlePricing {
	String getStoreCode();
	byte[] getArticleData(); 
	byte[] getPricingData();
	String getArtikelNummer();
	default ArticleDTO getArticle() {
		byte[] tempData = getArticleData();
		try {
			if (!Objects.isNull(tempData)) return (ArticleDTO)  deserialize(tempData);
		} catch (Exception e) {
		}
		return null;

	}
	default PricingDataDTO getPricing() {
		byte[] tempData = getPricingData();
		try {
			if (!Objects.isNull(tempData)) return (PricingDataDTO)  deserialize(tempData);
		} catch (Exception e) {
		}
		return null;

	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
}
