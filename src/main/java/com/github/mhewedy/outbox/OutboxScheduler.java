package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.framework.Advised;
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

    private static PositionedAdvisor ADVISOR_CACHE;

    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        outboxService.resetNonCompletedLockedOutbox();
    }

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
            Object[] paramValues = outbox.parseParamValues(objectMapper);
            Advised advised = (Advised) applicationContext.getBean(outbox.getServiceClass());

            var outboxAdvisor = removeOutboxAdvisor(advised);
            method.invoke(advised, paramValues);
            advised.addAdvisor(outboxAdvisor.pos(), outboxAdvisor.advisor());

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

    private PositionedAdvisor removeOutboxAdvisor(Advised advised) {
        if (ADVISOR_CACHE != null) {
            advised.removeAdvisor(ADVISOR_CACHE.pos());
            return ADVISOR_CACHE;
        }

        Advisor[] advisors = advised.getAdvisors();
        for (int i = 0; i < advisors.length; i++) {
            Advisor advisor = advisors[i];
            if (advisor instanceof PointcutAdvisor pointcutAdvisor) {
                if (pointcutAdvisor.getAdvice() instanceof AbstractAspectJAdvice aspectJAdvice) {
                    if (aspectJAdvice.getAspectName().equalsIgnoreCase(OutboxAspect.class.getSimpleName())) {
                        advised.removeAdvisor(advisor);
                        ADVISOR_CACHE = new PositionedAdvisor(i, advisor);
                        return ADVISOR_CACHE;
                    }
                }
            }
        }
        throw new RuntimeException("OutboxAspect advisor not found");
    }

    record PositionedAdvisor(int pos, Advisor advisor) {
    }
}