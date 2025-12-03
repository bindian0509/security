package com.bharat.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("contact")
public class ContactController {

    @GetMapping
    public String getContact() {
        return "Inquiry details are saved to the DB";
    }
}
