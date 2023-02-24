package com.github.mhewedy.outbox;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CalculatorService {

    @Outbox
    public void add(Integer a1, Point p) {
        log.info("result of add function: {}", a1 + p.x + p.y);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        public int x;
        public int y;
    }
}
