package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class WarningLevelService {

    private static final Duration MAX_AGE = Duration.ofMinutes(5);

    public WarningLevel calculate(Station station, Measurement measurement) {
        if (measurement == null || measurement.getTimestamp() == null) {
            return WarningLevel.UNKNOWN;
        }
        if (measurement.getTimestamp().isBefore(Instant.now().minus(MAX_AGE))) {
            return WarningLevel.UNKNOWN;
        }
        if (measurement.getWaterLevel() < 0) {
            return WarningLevel.UNKNOWN;
        }
        if (measurement.getWaterLevel() >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }
        if (measurement.getWaterLevel() >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }
        return WarningLevel.NORMAL;
    }
}