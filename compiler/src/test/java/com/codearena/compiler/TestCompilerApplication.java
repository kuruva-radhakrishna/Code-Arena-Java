package com.codearena.compiler;

import org.springframework.boot.SpringApplication;

public class TestCompilerApplication {

	public static void main(String[] args) {
		SpringApplication.from(CompilerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
