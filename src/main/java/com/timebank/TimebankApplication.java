package com.timebank;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
@MapperScan(basePackages = "com.timebank.mapper")
public class TimebankApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimebankApplication.class, args);
	}
}

