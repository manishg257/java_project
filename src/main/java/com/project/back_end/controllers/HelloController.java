package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String home() { return "Clinic API is running"; }

    @GetMapping("/health")
    public String health() { return "OK"; }
}
