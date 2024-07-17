package com.gerimedica.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/v1/")
public class HealthController {

    private static final Logger logger = LogManager.getLogger(HealthController.class);
    HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("fetchall")
    public ResponseEntity<Resource> getHealthMetrics() {
        String filename = healthService.healthMetricsList();
        if (Strings.isNotEmpty(filename)) {
            try {
                Path path = Paths.get(filename);
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=healthmetrics.csv");
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType("application/csv"))
                        .contentLength(resource.contentLength())
                        .body(resource);
            } catch (IOException ex) {
                logger.error("Failed to generate CSV file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ByteArrayResource(("Failed to generate CSV file").getBytes()));
            }
        } else
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ByteArrayResource(("Failed to generate CSV file").getBytes()));
    }

    @GetMapping("fetchbycode")
    public ResponseEntity<Resource> getHealthMetrics(@RequestParam("code") String code) {
        String filename = healthService.getHealthMetric(code);
        if (Strings.isNotEmpty(filename)) {
            try {
                Path path = Paths.get(filename);
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=healthmetrics.csv");
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType("application/csv"))
                        .contentLength(resource.contentLength())
                        .body(resource);
            } catch (IOException ex) {
                logger.error("Failed to generate CSV file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ByteArrayResource(("Failed to generate CSV file").getBytes()));
            }
        } else
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ByteArrayResource(("Failed to generate CSV file").getBytes()));
    }

    @PostMapping("upload")
    public ResponseEntity<Void> addHealthMetric(@RequestParam("file") MultipartFile file) {
        if (healthService.uploadHealthMetric(file))
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("delete")
    public ResponseEntity<Void> healthMetrics() {
        healthService.removeAllHealthMetrics();
        return ResponseEntity.ok().build();
    }
}
