package com.exerciting.Exerciting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class ExercitingApplication {
	public static void main(String[] args) {
		// LocalDateTime.now()를 쓰는 스케줄러·시간 검증이 서버 OS 시간대(컨테이너는 보통 UTC)에
		// 따라 9시간 어긋나지 않도록 애플리케이션 기준 시간대를 한국 시간으로 고정한다.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(ExercitingApplication.class, args);
	}
}