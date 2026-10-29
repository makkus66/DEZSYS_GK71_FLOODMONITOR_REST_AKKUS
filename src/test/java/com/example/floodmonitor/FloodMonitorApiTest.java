package com.example.floodmonitor;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SpringBootTest(properties = "simulation.enabled=false")
@AutoConfigureMockMvc
class FloodMonitorApiTest {

    private static final Instant T1 = Instant.parse("2026-05-01T08:00:00Z");
    private static final Instant T2 = Instant.parse("2026-05-01T10:00:00Z");
    private static final Instant T3 = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeasurementRepository measurementRepository;

    @BeforeEach
    void setUp() {
        measurementRepository.deleteAll();
        measurementRepository.save(measurement(T1, 300));
        measurementRepository.save(measurement(T2, 310));
        measurementRepository.save(measurement(T3, 320));
    }

    private Measurement measurement(Instant timestamp, double waterLevel) {
        return new Measurement("ST-001", timestamp, waterLevel, waterLevel * 2.5, 2.0, 12.0, 80,
                StationStatus.ONLINE, WarningLevel.NORMAL);
    }

    @Test
    void allStationsAsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value("ST-001"));
    }

    @Test
    void allStationsAsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/*/item[1]/id").string("ST-001"));
    }

    @Test
    void existingStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-001"))
                .andExpect(jsonPath("$.river").value("Donau"));
    }

    @Test
    void unknownStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Station ST-999 wurde nicht gefunden"));
    }

    @Test
    void latestMeasurement() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waterLevel").value(320.0));
    }

    @Test
    void filterByTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T09:00:00Z")
                        .param("to", "2026-05-01T11:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].waterLevel").value(310.0));
    }

    @Test
    void invalidTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T12:00:00Z")
                        .param("to", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unsupportedFormat() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void maxLimit() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements").param("limit", "1000"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements").param("limit", "1001"))
                .andExpect(status().isBadRequest());
    }
}