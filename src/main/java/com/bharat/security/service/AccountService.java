package com.bharat.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public Map<String, String> getAllAccounts() {
        logger.debug("Entering getAllAccounts()");

        Map<String, String> accounts = new HashMap<>();

        logger.debug("Initializing account data");
        accounts.put("Sita Ram Deewan Chand", "00420");
        accounts.put("Choley Bhature wala", "00421");
        accounts.put("Choley Kulche Wala", "00422");

        logger.info("Retrieved {} accounts successfully", accounts.size());
        logger.debug("Exiting getAllAccounts() with {} accounts", accounts.size());

        return accounts;
    }
}
