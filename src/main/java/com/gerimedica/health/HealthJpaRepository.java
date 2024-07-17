package com.gerimedica.health;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthJpaRepository extends JpaRepository<HealthMetric, String> {
    HealthMetric findByCode(String code);
}
