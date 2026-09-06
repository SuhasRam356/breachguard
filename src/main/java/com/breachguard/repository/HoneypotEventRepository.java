package com.breachguard.repository;

import com.breachguard.model.HoneypotEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface HoneypotEventRepository extends JpaRepository<HoneypotEvent, Long> {

    @Query("SELECT e FROM HoneypotEvent e JOIN e.canary c WHERE c.user.id = :userId ORDER BY e.createdAt DESC")
    List<HoneypotEvent> findByUserId(Long userId);

    Optional<HoneypotEvent> findFirstByCanaryIdOrderByCreatedAtDesc(Long canaryId);
}
