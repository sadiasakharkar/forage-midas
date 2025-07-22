package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Optional;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class TaskFourTests {
    static final Logger logger = LoggerFactory.getLogger(TaskFourTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    @Test
    void task_four_verifier() throws InterruptedException {
        // Populate users
        userPopulator.populate();

        // Load and send transactions from file
        String[] transactionLines = fileLoader.loadStrings("/test_data/alskdjfh.fhdjsk");
        if (transactionLines == null || transactionLines.length == 0) {
            logger.error("No transactions loaded from file!");
            return;
        }

        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Wait for async processing to complete
        Thread.sleep(2000);

        // Fetch Wilbur's balance from database
        Optional<UserRecord> wilburOpt = userRepository.findByNameIgnoreCase("wilbur");

        if (wilburOpt.isPresent()) {
            BigDecimal wilburBalance = wilburOpt.get().getBalance();
            logger.info("🐷 Wilbur's Final Balance: " + wilburBalance);
            System.out.println("🐷 Wilbur's Final Balance: " + wilburBalance);
        } else {
            logger.error("Wilbur not found!");
            System.out.println("❌ Wilbur not found!");
        }

        logger.info("✅ Task complete — exiting test.");
    }
}
