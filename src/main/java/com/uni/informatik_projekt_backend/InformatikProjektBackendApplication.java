package com.uni.informatik_projekt_backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InformatikProjektBackendApplication {
    private static final Logger LOGGER = LogManager.getLogger(InformatikProjektBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InformatikProjektBackendApplication.class, args);
        LOGGER.info("Swagger UI available at: http://localhost:1337/swagger-ui.html");
    }
}