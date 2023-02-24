package com.github.mhewedy.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {

    List<OutboxEntity> findAllByLockId(String lockId);

    @Modifying
    @Transactional
    @Query("update OutboxEntity set status = null where status = 0")
    void resetLockedOutbox();
}
