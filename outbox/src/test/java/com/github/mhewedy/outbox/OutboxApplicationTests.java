package com.github.mhewedy.outbox;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class OutboxApplicationTests {

    @Autowired
    private CalculatorService calculatorService;

    @Test
    @SneakyThrows
    void contextLoads() {

        calculatorService.add(1, new CalculatorService.Point(2, 3));
        Thread.sleep(2 * 1000);
        calculatorService.add(1, new CalculatorService.Point(2, 3));
        calculatorService.add(1, new CalculatorService.Point(2, 3));
        calculatorService.add(1, new CalculatorService.Point(2, 3));
        calculatorService.add(1, new CalculatorService.Point(2, 3));
        calculatorService.add(1, new CalculatorService.Point(2, 3));
        calculatorService.add(1, new CalculatorService.Point(2, 3));

        Thread.sleep(10 * 1000);
    }

}
