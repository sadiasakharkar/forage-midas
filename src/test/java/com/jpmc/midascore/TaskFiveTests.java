package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Balance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
public class TaskFiveTests {

    private static final Logger logger = LoggerFactory.getLogger(TaskFiveTests.class);

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private BalanceQuerier balanceQuerier;

    @Test
    void task_five_verifier() throws InterruptedException {
        // Step 1: Preload test users
        userPopulator.populate();

        // Step 2: Load and publish test transactions
        String[] transactionLines = fileLoader.loadStrings("/test_data/rueiwoqp.tyruei");
        for (String line : transactionLines) {
            kafkaProducer.send(line);
        }

        // Step 3: Wait for the transactions to process
        Thread.sleep(3000);

        // Step 4: Collect and print balances
        logger.info("----------------------------------------------------------");
        logger.info("submit the following output to complete the task (include begin and end output denotations)");

        StringBuilder output = new StringBuilder("\n---begin output ---\n");
        for (long userId = 0; userId <= 12; userId++) {
            Balance balance = balanceQuerier.query(userId);
            output.append(balance).append("\n");
        }
        output.append("---end output ---");

        // Final log
        logger.info(output.toString());
    }
}
