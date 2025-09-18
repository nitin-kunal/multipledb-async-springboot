package com.nitin.grok.dao.db2;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class Db2DatasourceConfiguration {

	@Bean
	@ConfigurationProperties("spring.datasource.second")
	DataSourceProperties db2DataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	DataSource configuredDb2DataSource() {
		return db2DataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Bean
	@Qualifier("db2JdbcTemplate")
	JdbcTemplate jdbceTemplate(@Qualifier("configuredDb2DataSource") DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate;

	}
}
