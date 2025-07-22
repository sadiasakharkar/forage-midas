package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.IncentiveRecord;
import com.jpmc.midascore.repository.IncentiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncentiveService {

    @Autowired
    private IncentiveRepository incentiveRepository;

    public double calculateIncentive(double amount) {
        return Math.round(amount * 0.02 * 100.0) / 100.0; // 2% incentive
    }

    public void recordIncentive(Long userId, double amount) {
        IncentiveRecord incentive = new IncentiveRecord(userId, amount);
        incentiveRepository.save(incentive);
    }
}
