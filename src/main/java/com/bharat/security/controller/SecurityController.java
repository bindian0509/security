package com.bharat.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {
    @GetMapping("welcome")
    public String welcome() {
        return "Welcome to Spring Boot Application";
    }
}
