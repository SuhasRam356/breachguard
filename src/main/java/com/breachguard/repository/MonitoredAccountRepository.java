package com.breachguard.repository;

import com.breachguard.model.MonitoredAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MonitoredAccountRepository extends JpaRepository<MonitoredAccount, Long> {
    List<MonitoredAccount> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<MonitoredAccount> findByUserIdAndAccount(Long userId, String account);
}
