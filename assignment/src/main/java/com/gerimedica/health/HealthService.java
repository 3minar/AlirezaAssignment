package com.gerimedica.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HealthService {

    private static final Logger logger = LogManager.getLogger(HealthService.class);
    HealthDao healthDao;

    public HealthService(HealthDao healthDao) {
        this.healthDao = healthDao;
    }

    public String healthMetricsList(){
        return generateCSV(healthDao.getAllHealthMetrics());
    }

    public String getHealthMetric(String code){
        return generateCSV(healthDao.findHealthMetricByCode(code));
    }

    public void removeAllHealthMetrics(){
        healthDao.deleteAll();
    }

    public boolean uploadHealthMetric(MultipartFile file){
        if (file.isEmpty()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            List<HealthMetric> healthMetrics = new ArrayList<>();
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] data = line.split(",");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                HealthMetric healthMetric = new HealthMetric();
                healthMetric.setSource(data[0].trim());
                healthMetric.setCodeListCode(data[1].trim());
                healthMetric.setCode(data[2].trim().replace("\"",""));
                healthMetric.setDisplayValue(data[3].trim());
                healthMetric.setLongDescription(data[4].trim());
                if (Strings.isNotEmpty(data[5].trim().replace("\"",""))) {
                    healthMetric.setFromDate(LocalDate.parse(data[5].trim().replace("\"",""), formatter));
                }
                if (Strings.isNotEmpty(data[6].trim().replace("\"",""))) {
                    healthMetric.setToDate(LocalDate.parse(data[6].trim().replace("\"",""), formatter));
                }
                if (Strings.isNotEmpty(data[7].trim().replace("\"",""))) {
                    healthMetric.setSortingPriority(Integer.parseInt(data[7].trim().replace("\"", "")));
                }
                healthMetrics.add(healthMetric);
            }
            healthDao.addHealthMetric(healthMetrics);
            return true;
        } catch (IOException | NumberFormatException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public String generateCSV(Object healthMetrics) {
        try {
            List<HealthMetric> metricsList;
            if (healthMetrics instanceof HealthMetric) {
                metricsList = Collections.singletonList((HealthMetric) healthMetrics);
            } else if (healthMetrics instanceof List<?>) {
                metricsList = (List<HealthMetric>) healthMetrics;
            } else {
                throw new IllegalArgumentException("Invalid input type");
            }
            String filename =  "src/main/resources/static/healthmetrics.csv";
            try (FileWriter fileWriter = new FileWriter(filename);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
                printWriter.println("Code,Source,CodeListCode,DisplayValue,LongDescription,FromDate,ToDate,SortingPriority");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                for (HealthMetric healthMetric : metricsList) {
                    printWriter.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                            healthMetric.getCode(),
                            healthMetric.getSource(),
                            healthMetric.getCodeListCode(),
                            healthMetric.getDisplayValue(),
                            healthMetric.getLongDescription(),
                            healthMetric.getFromDate() != null ? healthMetric.getFromDate().format(formatter) : "",
                            healthMetric.getToDate() != null ? healthMetric.getToDate().format(formatter) : "",
                            healthMetric.getSortingPriority() != null ? healthMetric.getSortingPriority() : "");
                }
            }
            return filename;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }
}
