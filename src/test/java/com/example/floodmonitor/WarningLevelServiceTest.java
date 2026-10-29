package com.example.floodmonitor;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.WarningLevelService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarningLevelServiceTest {

    private final WarningLevelService service = new WarningLevelService();

    // Warnwert 450 cm, kritischer Wert 600 cm
    private final Station station = new Station("ST-TEST", "Teststation", "Donau",
            new Location(48.0, 16.0), 280, 450, 600, true, StationStatus.ONLINE);

    private Measurement withWaterLevel(double waterLevel) {
        return new Measurement("ST-TEST", Instant.now(), waterLevel, 0, 0, 10, 100,
                StationStatus.ONLINE, null);
    }

    @Test
    void normalBelowWarningLevel() {
        assertEquals(WarningLevel.NORMAL, service.calculate(station, withWaterLevel(449.9)));
    }

    @Test
    void warningFromWarningLevel() {
        assertEquals(WarningLevel.WARNING, service.calculate(station, withWaterLevel(450)));
    }

    @Test
    void criticalFromCriticalLevel() {
        assertEquals(WarningLevel.CRITICAL, service.calculate(station, withWaterLevel(600)));
    }

    @Test
    void unknownWithoutMeasurement() {
        assertEquals(WarningLevel.UNKNOWN, service.calculate(station, null));
    }
}