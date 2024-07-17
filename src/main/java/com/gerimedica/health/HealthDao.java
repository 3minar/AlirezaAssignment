package com.gerimedica.health;

import java.util.List;

public interface HealthDao {
    void deleteAll();

    void addHealthMetric(List<HealthMetric> metrics);

    List<HealthMetric> getAllHealthMetrics();

    HealthMetric findHealthMetricByCode(String code);
}
