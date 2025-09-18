package com.nitin.grok.dao.db2.postgres;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgresDatasourceConfiguration {

	@Bean
	@ConfigurationProperties("spring.datasource.first")
	public DataSourceProperties postgresDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	DataSource configuredPostgresDataSource() {
		return postgresDataSourceProperties().initializeDataSourceBuilder().build();
	}

	@Bean
	@Qualifier("postgresJdbcTemplate")
	JdbcTemplate jdbcTemplate(@Qualifier("configuredPostgresDataSource") DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate;

	}
}
