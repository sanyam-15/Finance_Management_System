package com.syfe.financemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Personal Finance Manager API.
 */
@SpringBootApplication
public class FinanceManagerApplication {

	/**
	 * Starts the application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(FinanceManagerApplication.class, args);
	}

}
