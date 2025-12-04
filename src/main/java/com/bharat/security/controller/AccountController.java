package com.bharat.security.controller;

import com.bharat.security.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
        logger.info("AccountController initialized");
    }

    @GetMapping("{accountId}")
    public String getAccountDetails(@PathVariable String accountId) {
        logger.info("Received request to get account details for accountId: {}", accountId);

        String response = "The Account Id "+accountId+" has been closed due to fraud charges!";

        logger.debug("Returning account details response for accountId: {}", accountId);
        return response;
    }

    @GetMapping
    public Map<String, String> getAllAccounts() {
        logger.info("Received request to get all accounts");

        Map<String, String> accounts = accountService.getAllAccounts();

        logger.info("Successfully retrieved {} accounts", accounts.size());
        return accounts;
    }

}
