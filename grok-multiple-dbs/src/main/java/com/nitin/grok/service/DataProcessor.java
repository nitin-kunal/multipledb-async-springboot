package com.nitin.grok.service;


import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nitin.grok.exception.DataFetchException;
import com.nitin.grok.exception.ServiceCancellationException;
import com.nitin.grok.exception.ServiceTimeoutException;


@Component
public class DataProcessor {
	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);
    @Autowired
    private DataAsyncService dataAsyncService;

   //----------------------------this is used now ------------------------- 
    public CompletableFuture<ResponseEntity<String>> getCombinedDataRightNow() {
    	log.info("DataProcessor :: started");
    	CompletableFuture<Long> booksCountedFuture = dataAsyncService.getAllBooks();
    	CompletableFuture<Long> stateCountedFuture = dataAsyncService.getAllStates();//postgres
    	
    	return CompletableFuture.allOf(booksCountedFuture, stateCountedFuture)
    			.orTimeout(7, TimeUnit.SECONDS)
    			.thenApplyAsync(ignored -> {
    				try {
    					Long booksCount = booksCountedFuture.getNow(null);
    					Long stateCount = stateCountedFuture.getNow(null);
    					if (booksCount == null || stateCount == null) {
    						log.info("DataProcessor :: booksCount or stateCount is null!!");
    						return ResponseEntity.status(500).
    								body("Error: One or more database queries timed out or failed");
    					}
    					String response = "booksCount: " + booksCount.toString() + 
    							"\nstateCount: " + stateCount.toString();
    					log.info("DataProcessor :: done");
    					return ResponseEntity.ok(response);
    				} catch (Exception e) {
    					log.error("DataProcessor :: Error retrieving data: \" + e.getMessage()");
    					return ResponseEntity.status(500).body("Error retrieving data: " + e.getMessage());
    				}
    			}).exceptionally(ex -> {
    			    // unwrap nested CompletionException / ExecutionException chain to find root cause
    			    Throwable cause = ex;
    			    while ((cause instanceof CompletionException || cause instanceof ExecutionException) && cause.getCause() != null) {
    			        cause = cause.getCause();
    			    }

    			    // cancel children so long-running DB work can be interrupted
    			    booksCountedFuture.cancel(true);
    			    stateCountedFuture.cancel(true);

    			    // log full stack for diagnostics
    			   // log.error("DataProcessor :: composition failed: {}", cause.toString(), cause);

    			    // rethrow a specific unchecked exception so controller receives it as the top-level throwable
    			    if (cause instanceof TimeoutException) {
    			        throw new ServiceTimeoutException("Timeout while fetching data", cause);
    			    }
    			    if (cause instanceof CancellationException) {
    			        throw new ServiceCancellationException("Operation cancelled while fetching data", cause);
    			    }
    			    if (cause instanceof DataAccessException || cause instanceof IllegalStateException) {
    			        throw new DataFetchException("Database error while fetching data: " + cause.getMessage(), cause);
    			    }

    			    // fallback: wrap and rethrow so controller still sees a specific runtime exception
    			    throw new DataFetchException("Unexpected error while fetching data: " + cause.getMessage(), cause);
    			});
    }
  
}
