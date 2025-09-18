package com.nitin.grok.dao.db2.postgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresDAO {
	 Logger logger = LoggerFactory.getLogger(getClass());
	private final JdbcTemplate jdbc;

    public PostgresDAO(@Qualifier("postgresJdbcTemplate") JdbcTemplate  jdbc) {
        this.jdbc = jdbc;
    }

    
    public long countInPostgres() {
		
             
    	logger.info("PostgresDAO :: started books query");
        String sql = "SELECT COUNT(*) FROM books ";
        try {
            Long count = jdbc.queryForObject(sql, Long.class);
            logger.info("PostgresDAO :: query fired and postgres result is {} ", count);
            return count == null ? 0L : count;
        } catch (DataAccessException ex) {
        	logger.error("PostgresDAO :: unexpected error executing count query", 
        			  ex.getMessage());
        	 throw new IllegalStateException("Postgres Database operation failed while retrieving book count",
        			 ex);
        }
    }

}
