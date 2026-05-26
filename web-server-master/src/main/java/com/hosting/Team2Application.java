package com.hosting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // ✨ 비동기 임포트 추가

@EnableAsync // ✨ 백엔드 비동기 멀티스레드 활성화!
@SpringBootApplication
public class Team2Application {

	public static void main(String[] args) {
		SpringApplication.run(Team2Application.class, args);
	}

}