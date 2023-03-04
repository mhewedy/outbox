package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static com.github.mhewedy.outbox.OutboxEntity.Status;

@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;
    private final ApplicationContext applicationContext;

    @Scheduled(fixedRateString = "#{@'outbox-com.github.mhewedy.outbox.OutboxProperties'.schedulerFixedRate}")
    public void run() {
        log.trace("OutboxScheduler start running");

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
            // TODO implement retry
            Method method = outbox.getMethod();
            Object[] paramValues = outbox.parseParamValues(objectMapper);
            Advised advised = (Advised) applicationContext.getBean(outbox.getServiceClass());

            method.invoke(advised, paramValues);

            outbox.status = Status.SUCCESS;
            outboxService.update(outbox);

        } catch (Throwable ex) {
            if (ex instanceof InvocationTargetException itex) {
                ex = itex.getCause();
            }

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            log.warn(exceptionAsString);

            outbox.status = Status.FAIL;
            outbox.errorMessage = exceptionAsString;
            outboxService.update(outbox);
        }
    }
}
