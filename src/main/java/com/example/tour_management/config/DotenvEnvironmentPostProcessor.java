package com.example.tour_management.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> props = new HashMap<>();
        props.put("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID", ""));
        props.put("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET", ""));
        props.put("GEMINI_API_KEY", dotenv.get("GEMINI_API_KEY", ""));
        props.put("MAIL_USERNAME", dotenv.get("MAIL_USERNAME", ""));
        props.put("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD", ""));

        environment.getPropertySources()
                .addFirst(new MapPropertySource("dotenvProperties", props));
    }
}