package com.github.mhewedy.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OutboxService {

    private final JdbcTemplate jdbcTemplate;
    private final OutboxProperties outboxProperties;

    public List<OutboxDto> listByStatus(Status status) {
        return jdbcTemplate
                .query("select * from outbox_messages where status = ? ", OutboxEntity.ROW_MAPPER, status.ordinal())
                .stream()
                .map(OutboxDto::fromEntity)
                .toList();
    }

    /**
     * Set status of an outbox message to {@link Status#PENDING}, so to be picked by the scheduler.
     *
     * @param id the outbox id, returned from {@link #listByStatus(Status)}
     */
    public boolean setPending(String id) {
        return jdbcTemplate.update("update outbox_messages set status = ? where id = ?",
                Status.PENDING.ordinal(), id) > 0;
    }

    List<OutboxEntity> findAllByLockId(String lockId) {
        return jdbcTemplate.query("select * from outbox_messages where lock_id = ? ", OutboxEntity.ROW_MAPPER, lockId);
    }

    @Transactional
    boolean tryLock(String lockId) {

        int prefetchCount = outboxProperties.prefetchCount;

        int updated = jdbcTemplate.update("""
                        update outbox_messages
                                    set lock_id = ?,
                                        status = ?
                                    where id in (select top %d id from outbox_messages where status = ? )
                        """.formatted(prefetchCount),
                lockId,
                Status.LOCKED.ordinal(),
                Status.PENDING.ordinal()
        );

        return updated > 0;
    }

    @Transactional
    void save(OutboxEntity outbox) {
        jdbcTemplate.update("""
                        insert into outbox_messages (id, service_class, method_name, param_types, param_values,
                            lock_id, status, error_message, created_date)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID().toString(),
                outbox.serviceClass,
                outbox.methodName,
                outbox.paramTypes,
                outbox.paramValues,
                outbox.lockId,
                outbox.status.ordinal(),
                outbox.errorMessage,
                Timestamp.from(outbox.createdDate));
    }

    @Transactional
    void update(OutboxEntity outbox) {
        jdbcTemplate.update("""
                        update outbox_messages
                        set service_class = ?,
                        method_name = ?,
                        param_types = ?,
                        param_values = ?,
                        lock_id = ?,
                        status = ?,
                        error_message = ?,
                        modified_date = ?
                        where id = ?
                        """,
                outbox.serviceClass,
                outbox.methodName,
                outbox.paramTypes,
                outbox.paramValues,
                outbox.lockId,
                outbox.status.ordinal(),
                outbox.errorMessage,
                Timestamp.from(Instant.now()),
                outbox.id);
    }
}
