package com.uni.informatik_projekt_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InformatikProjektBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InformatikProjektBackendApplication.class, args);
        //noinspection JavaPrintToLogpoint
        System.out.println("Swagger UI available at: http://localhost:1337/swagger-ui.html");
    }
}