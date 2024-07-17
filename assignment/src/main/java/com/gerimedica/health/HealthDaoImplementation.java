package com.gerimedica.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HealthDaoImplementation implements HealthDao {
    private static final Logger logger = LogManager.getLogger(HealthDaoImplementation.class);
    HealthJpaRepository healthJpaRepository;

    public HealthDaoImplementation(HealthJpaRepository healthJpaRepository) {
        this.healthJpaRepository = healthJpaRepository;
    }

    @Override
    public void deleteAll() {
        healthJpaRepository.deleteAll();
    }

    @Override
    public void addHealthMetric(List<HealthMetric> metrics) {
        try {
            healthJpaRepository.saveAll(metrics);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public List<HealthMetric> getAllHealthMetrics() {
        return healthJpaRepository.findAll();
    }

    @Override
    public HealthMetric findHealthMetricByCode(String code) {
        return healthJpaRepository.findByCode(code);
    }
}
