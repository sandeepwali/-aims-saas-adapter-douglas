package com.solumesl.aims.saas.dou.batch.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;

import com.solumesl.aims.saas.dou.dto.DouglasSaasData;

public class CustomFieldExtractor<T> extends BeanWrapperFieldExtractor<T> {

	@Override
	public Object[] extract(T item) {
		
		DouglasSaasData data = (DouglasSaasData) item;
		Map<String, String> map = data.getData();
		List<Object> values = new ArrayList<>(map.values());
		return values.toArray();
	}
}
