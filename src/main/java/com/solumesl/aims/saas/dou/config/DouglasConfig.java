package com.solumesl.aims.saas.dou.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
/**
 * 
 * @author baskarmohanasundaram
 *
 */
@Configuration
@Getter
@Setter
public class DouglasConfig {
	
	@Value("${solum.saas.douglas.sftp.directory:C:\\WORK\\DOU\\data}")
	private String sftpDirectory;
	 

 


}
