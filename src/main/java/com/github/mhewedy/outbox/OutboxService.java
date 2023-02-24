package com.github.mhewedy.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final JdbcTemplate jdbcTemplate;
    private final OutboxRepository outboxRepository;

    public List<OutboxEntity> findAllByLockId(String lockId) {
        return outboxRepository.findAllByLockId(lockId);
    }

    @Transactional
    public boolean tryLock(String lockId) {

        int prefetchCount = 5;      // TODO read from props

        int updated = jdbcTemplate.update("""
                update outbox_messages
                            set lock_id = ?,
                                status = ?
                            where status is null
                            and id in (select top %d id from outbox_messages where status is null)
                """.formatted(prefetchCount), lockId, OutboxEntity.Status.LOCKED.ordinal());

        return updated > 0;
    }

    @Transactional
    public void update(OutboxEntity outbox) {
        outboxRepository.save(outbox);
    }
}
