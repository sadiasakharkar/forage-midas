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

import java.math.RoundingMode;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
})
public class TaskThreeTests {

    static final Logger logger = LoggerFactory.getLogger(TaskThreeTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    /**
     * This test triggers the transaction processing by sending messages to Kafka
     */
    @Test
    void task_three_verifier() throws InterruptedException {
        userPopulator.populate();
        String[] transactionLines = fileLoader.loadStrings("/test_data/mnbvcxz.vbnm");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        // Wait for processing to complete
        Thread.sleep(3000); // 3 seconds (adjust if needed)

        logger.info("----------------------------------------------------------");
        logger.info("Processing complete. Now run the printWaldorfBalance() test to find Waldorf's final balance.");
        logger.info("----------------------------------------------------------");
    }

    /**
     * This test prints the final balance of 'waldorf'
     */
    @Test
    public void printWaldorfBalance() {
        userRepository.findByName("waldorf").ifPresentOrElse(waldorf -> {
            System.out.println("💰 Final balance of Waldorf: " +
                    waldorf.getBalance().setScale(0, RoundingMode.DOWN));
        }, () -> {
            System.out.println("❌ Waldorf not found.");
        });
    }
}
