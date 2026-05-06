package com.example.movieservice;

import com.example.movieservice.model.User;
import com.example.movieservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
public class MovieServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, PasswordEncoder encoder) {
        return args -> {
            boolean adminExists = repository.findAll().stream()
                .anyMatch(u -> "ROLE_ADMIN".equals(u.getRole()));
            if (!adminExists) {
                User admin = new User();
                admin.setUsername("Admin");
                admin.setPassword(encoder.encode("123"));
                admin.setRole("ROLE_ADMIN");
                repository.save(admin);
                System.out.println("--- Главный админ создан ---");
            }
        };
    }
}

