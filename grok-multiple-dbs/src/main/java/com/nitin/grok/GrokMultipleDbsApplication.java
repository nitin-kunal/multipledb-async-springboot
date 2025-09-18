package com.nitin.grok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class GrokMultipleDbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrokMultipleDbsApplication.class, args);
	}

}
