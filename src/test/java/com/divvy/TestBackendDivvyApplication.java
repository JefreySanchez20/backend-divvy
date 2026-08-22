package com.divvy;

import org.springframework.boot.SpringApplication;

public class TestBackendDivvyApplication {

	public static void main(String[] args) {
		SpringApplication.from(BackendDivvyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
