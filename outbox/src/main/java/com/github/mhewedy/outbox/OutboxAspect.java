package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class OutboxAspect {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    @Around("@annotation(Outbox)")
    public Object sendToOutbox(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        if (method.getReturnType() != Void.class && method.getReturnType() != void.class) {
            throw new RuntimeException("method annotated with @Outbox should have void return type: %s".formatted(method));
        }

        boolean invokedFromScheduler = Arrays.stream((new Throwable()).getStackTrace())
                .parallel().map(StackTraceElement::toString)
                .anyMatch(it -> it.contains(OutboxScheduler.class.getName()));

        if (invokedFromScheduler) {
            joinPoint.proceed();
        } else {
            outboxService.save(OutboxEntity.create(objectMapper, method, Arrays.asList(joinPoint.getArgs())));
        }

        return null;
    }
}
