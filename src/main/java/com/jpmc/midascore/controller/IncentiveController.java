package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.IncentiveRepository;
import com.jpmc.midascore.entity.IncentiveRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incentives")
public class IncentiveController {

    @Autowired
    private IncentiveRepository incentiveRepository;

    @GetMapping("/balance/{userId}")
    public Balance getBalance(@PathVariable Long userId) {
        List<IncentiveRecord> incentives = incentiveRepository.findByUserId(userId);
        double total = incentives.stream().mapToDouble(IncentiveRecord::getAmount).sum();
        return new Balance(total);
    }
}
