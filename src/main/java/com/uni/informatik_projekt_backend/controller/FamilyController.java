package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.Family;
import com.uni.informatik_projekt_backend.repository.FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/families")
public class FamilyController {

    @Autowired
    private FamilyRepository familyRepository;

    @GetMapping
    public List<Family> getAllFamilies() {
        List<Family> families = familyRepository.findAll();

        // This will print the data directly to your IntelliJ Run console when someone visits the route
        System.out.println("Fetched families from database: " + families.size());

        return families; // Automatically converted to JSON for your Flutter app
    }
}