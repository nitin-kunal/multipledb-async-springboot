package com.nitin.grok.controller;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return dataProcessor.getCombinedData();
    }
}
