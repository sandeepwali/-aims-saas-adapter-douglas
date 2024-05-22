package com.solumesl.aims.saas.dou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.solumesl"})
public class AimsSaasDouglasAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(AimsSaasDouglasAdapterApplication.class, args);
	}

}
