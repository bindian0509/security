package com.bharat.security.controller;

import com.bharat.security.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    public AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("{accountId}")
    public String getAccountDetails(@PathVariable String accountId) {

        return "The Account Id "+accountId+" has been closed due to fraud charges!";
    }

    @GetMapping
    public Map<String, String> getAllAccounts() {
        return accountService.getAllAccounts();
    }

}
