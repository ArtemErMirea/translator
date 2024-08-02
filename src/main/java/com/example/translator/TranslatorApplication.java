package com.example.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class TranslatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranslatorApplication.class, args);
	}

}
