package com.gerimedica.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthServiceTest {

    @Mock
    private HealthDao healthDao;

    @InjectMocks
    private HealthService healthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void healthMetricsList() {
        // Arrange
        List<HealthMetric> metrics = new ArrayList<>();
        metrics.add(new HealthMetric("code1", "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1));
        when(healthDao.getAllHealthMetrics()).thenReturn(metrics);

        // Act
        String csvFilePath = healthService.healthMetricsList();

        // Assert
        assertNotNull(csvFilePath);
        verify(healthDao, times(1)).getAllHealthMetrics();
    }

    @Test
    void getHealthMetric() {
        // Arrange
        String code = "code1";
        HealthMetric metric = new HealthMetric(code, "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1);
        when(healthDao.findHealthMetricByCode(code)).thenReturn(metric);

        // Act
        String csvFilePath = healthService.getHealthMetric(code);

        // Assert
        assertNotNull(csvFilePath);
        verify(healthDao, times(1)).findHealthMetricByCode(code);
    }

    @Test
    void removeAllHealthMetrics() {
        // Act
        healthService.removeAllHealthMetrics();

        // Assert
        verify(healthDao, times(1)).deleteAll();
    }

    @Test
    void uploadHealthMetric() throws IOException {
        // Arrange
        String csvContent = "Source,CodeListCode,Code,DisplayValue,LongDescription,FromDate,ToDate,SortingPriority\n" +
                "source1,codeList1,code1,displayValue1,longDescription1,01-01-2021,31-12-2021,1\n";
        InputStream is = new ByteArrayInputStream(csvContent.getBytes());
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", is);

        // Act
        boolean result = healthService.uploadHealthMetric(file);

        // Assert
        assertTrue(result);

        ArgumentCaptor<List<HealthMetric>> captor = ArgumentCaptor.forClass(List.class);
        verify(healthDao, times(1)).addHealthMetric(captor.capture());
        List<HealthMetric> capturedMetrics = captor.getValue();
        assertEquals(1, capturedMetrics.size());

        HealthMetric metric = capturedMetrics.get(0);
        assertEquals("source1", metric.getSource());
        assertEquals("codeList1", metric.getCodeListCode());
        assertEquals("code1", metric.getCode());
        assertEquals("displayValue1", metric.getDisplayValue());
        assertEquals("longDescription1", metric.getLongDescription());
        assertEquals(LocalDate.of(2021, 1, 1), metric.getFromDate());
        assertEquals(LocalDate.of(2021, 12, 31), metric.getToDate());
        assertEquals(1, metric.getSortingPriority());
    }

    @Test
    void generateCSV() {
        // Arrange
        List<HealthMetric> metrics = new ArrayList<>();
        metrics.add(new HealthMetric("code1", "source1", "codeList1", "displayValue1", "longDescription1", LocalDate.now(), LocalDate.now().plusDays(1), 1));

        // Act
        String csvFilePath = healthService.generateCSV(metrics);

        // Assert
        assertNotNull(csvFilePath);
        assertTrue(csvFilePath.endsWith(".csv"));
    }
}
