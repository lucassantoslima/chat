package com.chat.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@SpringBootApplication
public class LucasLimaChatApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LucasLimaChatApplication.class, args);
	}
	
}
