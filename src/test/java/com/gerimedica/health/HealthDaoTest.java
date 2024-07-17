package com.gerimedica.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HealthDaoTest {

    @Mock
    private HealthJpaRepository healthJpaRepository;

    @InjectMocks
    private HealthDaoImplementation healthDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteAll() {
        // Act
        healthDao.deleteAll();

        // Assert
        verify(healthJpaRepository, times(1)).deleteAll();
    }

    @Test
    void addHealthMetric() {
        // Arrange
        List<HealthMetric> metrics = new ArrayList<>();
        metrics.add(new HealthMetric("code1", "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1));
        metrics.add(new HealthMetric("code2", "source2", "codeList2", "displayValue2", "longDescription2", LocalDate.now(), LocalDate.now().plusDays(2), 2));

        // Act
        healthDao.addHealthMetric(metrics);

        // Assert
        ArgumentCaptor<List<HealthMetric>> captor = ArgumentCaptor.forClass(List.class);
        verify(healthJpaRepository, times(1)).saveAll(captor.capture());
        assertEquals(metrics, captor.getValue());
    }

    @Test
    void getAllHealthMetrics() {
        // Arrange
        List<HealthMetric> expectedMetrics = new ArrayList<>();
        expectedMetrics.add(new HealthMetric("code1", "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1));
        expectedMetrics.add(new HealthMetric("code2", "source2", "codeList2", "displayValue2", "longDescription2", LocalDate.now(), LocalDate.now().plusDays(2), 2));

        when(healthJpaRepository.findAll()).thenReturn(expectedMetrics);

        // Act
        List<HealthMetric> actualMetrics = healthDao.getAllHealthMetrics();

        // Assert
        assertEquals(expectedMetrics, actualMetrics);
        verify(healthJpaRepository, times(1)).findAll();
    }

    @Test
    void findHealthMetricByCode() {
        // Arrange
        String code = "code1";
        HealthMetric expectedMetric = new HealthMetric(code, "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1);
        when(healthJpaRepository.findByCode(code)).thenReturn(expectedMetric);

        // Act
        HealthMetric actualMetric = healthDao.findHealthMetricByCode(code);

        // Assert
        assertEquals(expectedMetric, actualMetric);
        verify(healthJpaRepository, times(1)).findByCode(code);
    }
}
