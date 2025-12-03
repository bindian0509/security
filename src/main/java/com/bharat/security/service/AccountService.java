package com.bharat.security.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    public Map<String, String> getAllAccounts() {

        Map<String, String> accounts = new HashMap<>();

        accounts.put("Sita Ram Deewan Chand", "00420");
        accounts.put("Choley Bhature wala", "00421");
        accounts.put("Choley Kulche Wala", "00422");

        return accounts;
    }
}
