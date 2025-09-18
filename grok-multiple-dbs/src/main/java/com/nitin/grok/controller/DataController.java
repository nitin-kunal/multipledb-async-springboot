package com.nitin.grok.controller;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nitin.grok.exception.DataFetchException;
import com.nitin.grok.exception.ServiceCancellationException;
import com.nitin.grok.exception.ServiceTimeoutException;
import com.nitin.grok.service.DataProcessor;

@RestController
@RequestMapping("/api")
public class DataController {
	private static final Logger log = LoggerFactory.getLogger(DataController.class);
    
	@Autowired
    private DataProcessor dataProcessor;
    
     @GetMapping("/data")
    public CompletableFuture<ResponseEntity<String>> getCombinedData() {
        log.info("DataController :: started");

        return dataProcessor.getCombinedDataRightNow()
        		.handle((resp, throwable) -> {
        	        if (throwable == null) return resp;

        	        // no need to unwrap CompletionException; check the runtime types you threw
        	        if (throwable instanceof ServiceTimeoutException) {
        	            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("Timeout: " + throwable.getMessage());
        	        }
        	        if (throwable instanceof ServiceCancellationException) {
        	            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Cancelled: " + throwable.getMessage());
        	        }
        	        if (throwable instanceof DataFetchException) {
        	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB error: " + throwable.getMessage());
        	        }
        	        // fallback
        	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + throwable.getMessage());
        	    });

    }


}
