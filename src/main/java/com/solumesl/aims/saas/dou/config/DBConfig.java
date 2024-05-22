package com.solumesl.aims.saas.dou.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories({"com.solumesl.aims.saas.dou"})
@EnableJpaAuditing
public class DBConfig {

}
