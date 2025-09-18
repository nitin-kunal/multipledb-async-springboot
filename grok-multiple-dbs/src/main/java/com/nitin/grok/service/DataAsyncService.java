package com.nitin.grok.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nitin.grok.dao.db2.Db2DAO;
import com.nitin.grok.dao.db2.postgres.PostgresDAO;

@Service
public class DataAsyncService {
	private static final Logger log = LoggerFactory.getLogger(DataAsyncService.class);

    @Autowired
    private PostgresDAO postgresDao;

    @Autowired
    private Db2DAO db2dao;

    @Async("custom-executor")
    public CompletableFuture<Long> getAllStates() {
    	log.info("DataAsyncService-> getAllStates :: started");
        return CompletableFuture.supplyAsync(() -> postgresDao.countInPostgres())
                .orTimeout(5, TimeUnit.SECONDS);
    }

    @Async("custom-executor")
    public CompletableFuture<Long> getAllBooks() {
    	log.info("DataAsyncService-> getAllBooks :: started");
        return CompletableFuture.supplyAsync(() -> db2dao.countInDB2())
                .orTimeout(5, TimeUnit.SECONDS);
    }
}
