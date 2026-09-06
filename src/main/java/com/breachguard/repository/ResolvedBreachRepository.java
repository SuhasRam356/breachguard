package com.breachguard.repository;

import com.breachguard.model.ResolvedBreach;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResolvedBreachRepository extends JpaRepository<ResolvedBreach, Long> {
    List<ResolvedBreach> findByUserIdAndAccount(Long userId, String account);
    Optional<ResolvedBreach> findByUserIdAndAccountAndBreachName(Long userId, String account, String breachName);
    long countByUserId(Long userId);
}
