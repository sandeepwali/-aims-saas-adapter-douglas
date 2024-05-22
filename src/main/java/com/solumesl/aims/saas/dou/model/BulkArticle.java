package com.solumesl.aims.saas.dou.model;

import lombok.Data;

@Data
public class BulkArticle {
	
	private String contentType = "zip";
	private String articleFileName;
	private String zipFileBase64;
	 
}
