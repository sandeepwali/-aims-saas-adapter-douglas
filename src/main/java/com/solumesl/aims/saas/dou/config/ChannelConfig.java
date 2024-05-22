package com.solumesl.aims.saas.dou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelConfig {

	@Bean(name ="prepareSaaSDatFile")
	public MessageChannel prepareSaaSDatFile() {
		return new DirectChannel();
	}

 
	@Bean
	public MessageChannel loadArticleMasterDataFile() {
		return new DirectChannel();
	}

}
