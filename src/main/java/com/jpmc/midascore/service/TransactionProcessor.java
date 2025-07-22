package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private IncentiveService incentiveService;

    public void process(String transactionLine) {
        try {
            // Parse line: e.g., 3,2,100.50
            String[] parts = transactionLine.split(",");
            long senderId = Long.parseLong(parts[0].trim());
            long recipientId = Long.parseLong(parts[1].trim());
            BigDecimal amount = new BigDecimal(parts[2].trim());

            Optional<UserRecord> senderOpt = userRepository.findById(senderId);
            Optional<UserRecord> recipientOpt = userRepository.findById(recipientId);

            if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
                logger.warn("❌ User not found — skipping transaction: {}", transactionLine);
                return;
            }

            UserRecord sender = senderOpt.get();
            UserRecord recipient = recipientOpt.get();

            if (sender.getBalance().compareTo(amount) < 0) {
                logger.warn("❌ Insufficient balance, ignored: Transaction {{senderId={}, recipientId={}, amount={}}}",
                        senderId, recipientId, amount);
                return;
            }

            // Deduct from sender
            sender.setBalance(sender.getBalance().subtract(amount));
            userRepository.save(sender);

            // Credit to recipient
            recipient.setBalance(recipient.getBalance().add(amount));

            // Calculate incentive and apply
            double incentiveAmount = incentiveService.calculateIncentive(amount.doubleValue());
            recipient.setBalance(recipient.getBalance().add(BigDecimal.valueOf(incentiveAmount)));
            userRepository.save(recipient);

            // Save transaction record
            TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, amount);
            transactionRecordRepository.save(transactionRecord);

            // Record incentive
            incentiveService.recordIncentive(recipient.getId(), incentiveAmount);

            logger.info("✅ Transaction saved: {} | Incentive: {}", transactionRecord, incentiveAmount);
        } catch (Exception e) {
            logger.error("❌ Failed to process transaction: {}", transactionLine, e);
        }
    }
}
