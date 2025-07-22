package com.jpmc.midascore.listener;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public TransactionListener(
            UserRepository userRepository,
            TransactionRecordRepository transactionRecordRepository,
            RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "group_id")
    @Transactional
    public void listen(Transaction transaction) {
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            logger.warn("❌ Invalid sender or recipient: {}", transaction);
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();
        BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

        if (sender.getBalance().compareTo(amount) < 0) {
            logger.warn("❌ Insufficient balance, ignored: {}", transaction);
            return;
        }

        // Call Incentive API
        String url = "http://localhost:8080/incentive";
        Incentive incentiveResponse = restTemplate.postForObject(url, transaction, Incentive.class);
        double incentiveAmount = (incentiveResponse != null) ? incentiveResponse.getAmount() : 0.0;
        BigDecimal incentive = BigDecimal.valueOf(incentiveAmount);

        // Deduct from sender
        sender.setBalance(sender.getBalance().subtract(amount));

        // Add to recipient with incentive
        recipient.setBalance(recipient.getBalance().add(amount).add(incentive));

        // Save updated users
        userRepository.save(sender);
        userRepository.save(recipient);

        // Save transaction record
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, amount);
        transactionRecordRepository.save(transactionRecord);

        logger.info("✅ Transaction saved: {} | Incentive: {}", transaction, incentiveAmount);
    }
}
