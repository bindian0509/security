package com.bharat.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("card")
public class CardController {

    @GetMapping("{accountId}")
    public String getCardsForUser (@PathVariable String accountId) {
        return "No cards for user : "+accountId;
    }
}
