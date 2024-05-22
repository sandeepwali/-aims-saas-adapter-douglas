package com.solumesl.aims.saas.dou.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class DouglasSaasData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3456237851131088073L;

	private Map<String, String> data = new LinkedHashMap<String, String>();

	public void addRecord(String key, String value) {
		this.data.put(key, value);
	}







}
