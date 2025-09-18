package com.nitin.grok.dao.db2;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Db2DAO {
	Logger logger = LoggerFactory.getLogger(getClass());
	private static final Logger log = LoggerFactory.getLogger(Db2DAO.class);
	private final JdbcTemplate jdbc;

	public Db2DAO(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Autowired
	StateRepository stateRepository;

	public long countInDB2() {
		logger.info("DB2DAO :: started state query");
		try {
			int result = stateRepository.findAll().size();
			logger.info("DB2DAO :: query fired and DB2 result is {} ", result);
			return result == 0 ? 0L : result;
		} catch (DataAccessException ex) {
			log.error("DB2 :: ERORR " + ex.getMessage());
			throw new IllegalStateException("error in :: DB2 QUERY", ex);

		}
	}
//-----------not working----------------
	public long countInDB21() {
		logger.info("DB2DAO :: started state query");
		// String sql = "select count(*) from STATE";
		String sql = "select count(*) from APPUSER.STATE";

		try {

			Long count = jdbc.queryForObject(sql, Long.class);
			logger.info("DB2DAO :: query fired and DB2 result is {} ", count);
			return count == null ? 0L : count;
		} catch (DataAccessException ex) {
			log.error("DB2DAO :: ERORR " + ex.getMessage());
			throw new IllegalStateException("error in :: DB2DAO QUERY", ex);

		}
	}

}
