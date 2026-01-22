package com.cencops.demo;

import com.cencops.demo.entity.User;
import com.cencops.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner init(
            UserRepository userRepository,
            @Value("${app.superadmin.password.hash}") String passwordHash
    ) {
        return args -> {
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                userRepository.save(User.builder()
                        .name("Super Admin")
                        .username("superadmin")
                        .designation("Super Admin")
                        .status(User.Status.ACTIVE)
                        .password(passwordHash)
                        .role(User.Role.SUPER_ADMIN)
                        .createdBy("SYSTEM")
                        .updatedBy("SYSTEM")
                        .build());
            }
        };
    }


}
