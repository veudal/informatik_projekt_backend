package com.uni.informatik_projekt_backend;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import javax.swing.JOptionPane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
public class InformatikProjektBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InformatikProjektBackendApplication.class, args);
        System.out.println("Swagger UI available at: http://localhost:1337/swagger-ui.html");
    }
}