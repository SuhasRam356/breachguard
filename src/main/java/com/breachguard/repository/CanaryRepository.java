package com.breachguard.repository;

import com.breachguard.model.Canary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CanaryRepository extends JpaRepository<Canary, Long> {
    Optional<Canary> findByTrapToken(String trapToken);
}
