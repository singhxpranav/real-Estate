package com.example.module_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.example.module_a", "com.example.module_b" })
public class ModuleAApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleAApplication.class, args);
	}

}
