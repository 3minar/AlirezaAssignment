package com.gerimedica.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@WebFluxTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HealthService healthService;

    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("test", ".csv");
        String csvContent = "source,codeListCode,code,displayValue,longDescription,fromDate,toDate,sortingPriority\n" +
                "source1,codeList1,code1,displayValue1,longDescription1,2020-01-01,2020-12-31,1\n" +
                "source2,codeList2,code2,displayValue2,longDescription2,2021-01-01,2021-12-31,2";
        Files.write(tempFile, csvContent.getBytes());
    }

    @Test
    void addHealthMetric() {
        given(healthService.uploadHealthMetric(any(MultipartFile.class))).willReturn(true);

        byte[] fileContent = ("source,codeListCode,code,displayValue,longDescription,fromDate,toDate,sortingPriority\n" +
                "source1,codeList1,code1,displayValue1,longDescription1,2020-01-01,2020-12-31,1").getBytes();

        webTestClient.post().uri("/api/v1/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new ByteArrayResource(fileContent)))
                .exchange()
                .expectStatus().isOk();
    }

     @Test
    void getHealthMetrics() {
        given(healthService.healthMetricsList()).willReturn(tempFile.toString());

        webTestClient.get().uri("/api/v1/fetchall")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.parseMediaType("application/csv"))
                .expectHeader().valueEquals("Content-Disposition", "attachment; filename=healthmetrics.csv")
                .expectBody(ByteArrayResource.class)
                .consumeWith(response -> {
                    ByteArrayResource resource = response.getResponseBody();
                    assertNotNull(resource);
                    assertArrayEquals("header1,header2\nvalue1,value2".getBytes(), resource.getByteArray());
                });
    }

    @Test
    void testGetHealthMetrics() {
        given(healthService.getHealthMetric(anyString())).willReturn(tempFile.toString());

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/api/v1/fetchbycode").queryParam("code", "some-code").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.parseMediaType("application/csv"))
                .expectHeader().valueEquals("Content-Disposition", "attachment; filename=healthmetrics.csv")
                .expectBody(ByteArrayResource.class)
                .consumeWith(response -> {
                    ByteArrayResource resource = response.getResponseBody();
                    assertNotNull(resource);
                    assertArrayEquals("header1,header2\nvalue1,value2".getBytes(), resource.getByteArray());
                });
    }

    @Test
    void healthMetrics() {
        webTestClient.delete().uri("/api/v1/delete")
                .exchange()
                .expectStatus().isOk();
    }
}