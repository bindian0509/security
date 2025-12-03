package com.bharat.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("balance")
public class BalanceController {

    @GetMapping("{accountId}")
    public String getBalance(@PathVariable String accountId) {
        return "Balance is always zero in your account : "+accountId;
    }
}
