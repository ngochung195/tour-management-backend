package com.example.tour_management;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TourManagementApplication {

	static {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID", ""));
		System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET", ""));
		System.setProperty("GEMINI_API_KEY", dotenv.get("GEMINI_API_KEY", ""));
		System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME", ""));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD", ""));
	}

	public static void main(String[] args) {

		SpringApplication.run(TourManagementApplication.class, args);

	}

}
