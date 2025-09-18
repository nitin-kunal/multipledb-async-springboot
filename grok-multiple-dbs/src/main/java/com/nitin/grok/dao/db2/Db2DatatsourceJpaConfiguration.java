package com.nitin.grok.dao.db2;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.nitin.grok.dao.db2", 
entityManagerFactoryRef = "writingEntityManagerFactory", 
transactionManagerRef = "writingTransactionManager")
public class Db2DatatsourceJpaConfiguration {
	@Bean
	LocalContainerEntityManagerFactoryBean writingEntityManagerFactory(
			@Qualifier("configuredDb2DataSource") DataSource dataSource, 
			EntityManagerFactoryBuilder builder) {
		return builder.dataSource(dataSource).packages("com.nitin.grok.dao.db2").build();
	}

	@Bean
	PlatformTransactionManager writingTransactionManager(
			@Qualifier("writingEntityManagerFactory") 
			LocalContainerEntityManagerFactoryBean 
			writingEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(writingEntityManagerFactory.getObject()));
	}
	 
	   
	   
}
