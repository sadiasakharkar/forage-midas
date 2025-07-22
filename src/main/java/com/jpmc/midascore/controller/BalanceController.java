package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Balance getBalance(@RequestParam String userId) {
        return userRepository.findByNameIgnoreCase(userId)
                .map(user -> new Balance(user.getBalance().doubleValue()))
                .orElse(new Balance(0));
    }
}
