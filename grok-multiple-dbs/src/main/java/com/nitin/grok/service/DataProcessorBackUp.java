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
public class DataProcessorBackUp {
	private static final Logger log = LoggerFactory.getLogger(DataProcessorBackUp.class);
    @Autowired
    private DataAsyncService dataAsyncService;
//------------------impl 1-------------------------------------
    public CompletableFuture<ResponseEntity<String>> getCombinedData1() {
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
                });
    }
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
    
	/*
	 * return CompletableFuture.allOf(fA, fB) .thenApply(voidResult -> { long a =
	 * fA.getNow(-1L); long b = fB.getNow(-1L);
	 * log.info("Both DB calls succeeded. Postgres={}, DB2={}", a, b); return new
	 * Result(a, b); }) .exceptionally(ex -> { Throwable cause = ex.getCause() !=
	 * null ? ex.getCause() : ex; log.error("Error fetching counts: {}",
	 * cause.getMessage(), cause); throw new
	 * IllegalStateException("Failed to fetch counts", cause); });
	 */
    
 //getCombinedData--------------------impl 1 ends---------------------------------------   
    //--------------------impl 2 starts---------------------------------------   
  
    public CompletableFuture<ResponseEntity<String>> getCombinedData() {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info("DataProcessor :: started");

        CompletableFuture<Long> booksCountedFuture = dataAsyncService.getAllBooks();
        CompletableFuture<Long> stateCountedFuture = dataAsyncService.getAllStates();

        CompletableFuture<Void> all = CompletableFuture.allOf(booksCountedFuture, stateCountedFuture)
                .orTimeout(7, TimeUnit.SECONDS);

        // cancel children immediately when all completes exceptionally
        CompletableFuture<Void> allWithCancellation = all.whenComplete((v, t) -> {
            if (t != null) {
                Throwable cause = (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
                log.warn("DataProcessor :: composition failed or timed out: {}", cause.toString(), cause);
                booksCountedFuture.cancel(true);
                stateCountedFuture.cancel(true);
            }
        });

        return allWithCancellation.thenApplyAsync(ignored -> {
            try {
                // join is safe here because all completed normally
                Long booksCount = booksCountedFuture.join();
                Long stateCount = stateCountedFuture.join();

                if (booksCount == null || stateCount == null) {
                    log.warn("DataProcessor :: booksCount or stateCount is null");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: One or more database queries returned null");
                }

                String response = "booksCount: " + booksCount + "\nstateCount: " + stateCount;
                log.info("DataProcessor :: done");
                return ResponseEntity.ok(response);

            } catch (CompletionException ce) {
                Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
                log.error("DataProcessor :: failure when joining futures", cause);
                if (cause instanceof java.util.concurrent.TimeoutException) {
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                            .body("Error: Timeout while fetching data");
                }
                if (cause instanceof CancellationException) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body("Error: Operation cancelled");
                }
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Error fetching data: " + cause.getMessage());
            } catch (Exception e) {
                log.error("DataProcessor :: unexpected error", e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Error fetching data: " + e.getMessage());
            }
        });
    }
    
    
 //------------------impl 2 ends-----------------------------------------------
    //------------------impl 3 starts-----------------------------------------------
 //--new implemenation with exception 
    public CompletableFuture<ResponseEntity<String>> getCombinedDataWithExceptionHandling() {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info("DataProcessor :: started");

        CompletableFuture<Long> booksCountedFuture = dataAsyncService.getAllBooks();
        CompletableFuture<Long> stateCountedFuture = dataAsyncService.getAllStates();

        return CompletableFuture.allOf(booksCountedFuture, stateCountedFuture)
            .orTimeout(7, TimeUnit.SECONDS)
            .thenApplyAsync(ignored -> {
                try {
                    Long booksCount = booksCountedFuture.join();
                    Long stateCount = stateCountedFuture.join();
                    if (booksCount == null || stateCount == null) {
                        log.warn("DataProcessor :: booksCount or stateCount is null");
                        return ResponseEntity.status(500).body("Error: Null data returned");
                    }
                    String response = "booksCount: " + booksCount + "\nstateCount: " + stateCount;
                    log.info("DataProcessor :: done");
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    log.error("DataProcessor :: Error: {}", e.getMessage());
                    return ResponseEntity.status(500).body("Error: " + e.getMessage());
                }
            })
            .exceptionally(throwable -> {
                if (throwable instanceof TimeoutException) {
                    log.error("DataProcessor :: Timeout after 7 seconds");
                    return ResponseEntity.status(504).body("Error: Timeout after 7 seconds");
                }
                log.error("DataProcessor :: Error: {}", throwable.getMessage());
                return ResponseEntity.status(500).body("Error: " + throwable.getMessage());
            })
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    booksCountedFuture.cancel(true);
                    stateCountedFuture.cancel(true);
                    log.debug("DataProcessor :: Futures cancelled");
                }
            });
    }
    
    
}
