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
class OutboxAspect {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    @Around("@annotation(Outbox)")
    public Object sendToOutbox(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        if (method.getReturnType() != Void.class && method.getReturnType() != void.class) {
            throw new RuntimeException("method annotated with @Outbox should have void return type: %s".formatted(method));
        }

        if (isInvokedFromScheduler()) {
            joinPoint.proceed();
        } else {
            outboxService.save(OutboxEntity.create(objectMapper, method, Arrays.asList(joinPoint.getArgs())));
        }

        return null;
    }

    private static boolean isInvokedFromScheduler() {
        var schedulerIndex = 20;
        StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
        if (stackTrace.length >= schedulerIndex + 1) {
            if (stackTrace[schedulerIndex].getClassName().equals(OutboxScheduler.class.getName())) {
                return true;
            }
        }
        return Arrays.toString(stackTrace).contains(OutboxScheduler.class.getName());
    }
}
