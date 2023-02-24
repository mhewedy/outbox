package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;
    private final ApplicationContext applicationContext;

    @Scheduled(fixedRate = 1000)
    public void run() {
        var lockId = UUID.randomUUID().toString();

        if (outboxService.tryLock(lockId)) {
            List<OutboxEntity> outboxListIds = outboxService.findAllByLockId(lockId);
            outboxListIds.forEach(this::processOutbox);
        } else {
            log.trace("no new messages found.");
        }
    }

    private void processOutbox(OutboxEntity outbox) {
        log.info("processing outbox with id: {}", outbox.id);
        try {
            Method method = outbox.getMethod();
            Object bean = AopProxyUtils.getSingletonTarget(applicationContext.getBean(outbox.getServiceClass()));
            Object[] paramValues = outbox.parseParamValues(objectMapper);

            method.invoke(bean, paramValues);

            outbox.status = OutboxEntity.Status.SUCCESS;
            outboxService.update(outbox);

        } catch (Throwable ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            log.warn(exceptionAsString);

            outbox.status = OutboxEntity.Status.FAIL;
            outbox.errorMessage = exceptionAsString;
            outboxService.update(outbox);
        }
    }
}
