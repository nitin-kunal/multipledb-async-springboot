package com.nitin.grok.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nitin.grok.controller.DataController;

@Component
public class DataProcessor {
	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);
    @Autowired
    private DataAsyncService dataAsyncService;

    public CompletableFuture<ResponseEntity<String>> getCombinedData() {
    	log.info("DataProcessor :: started");
        CompletableFuture<Long> booksCountedFuture = dataAsyncService.getAllBooks();
        CompletableFuture<Long> stateCountedFuture = dataAsyncService.getAllStates();

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
                });
    }
}
