package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Balance;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BalanceQuerier {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BALANCE_API = "http://localhost:8080/api/incentives/balance/";

    public Balance query(Long userId) {
        return restTemplate.getForObject(BALANCE_API + userId, Balance.class);
    }
}
