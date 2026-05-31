package com.hosting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 쉘 스크립트 비동기 실행을 위해 필수!
@SpringBootApplication
public class Team2Application {
	public static void main(String[] args) {
		SpringApplication.run(Team2Application.class, args);
	}
}