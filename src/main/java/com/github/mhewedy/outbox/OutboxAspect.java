package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mhewedy.outbox.cdc.DebeziumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OutboxAspect {

    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @Around("@annotation(Outbox)")
    public Object sendToOutbox(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        if (method.getReturnType() != Void.class && method.getReturnType() != void.class) {
            throw new RuntimeException("method annotated with @Outbox should have void return type: %s".formatted(method));
        }

        Long id = outboxRepository
                .save(OutboxEntity.create(objectMapper, method, Arrays.asList(joinPoint.getArgs())))
                .id;

        if (DebeziumUtil.isDebeziumPresent()) {
            // deleting the record in case of debezium
            // because we interested in the transaction log and not in the actual record.
            outboxRepository.deleteById(id);
        }

        return null;
    }
}
