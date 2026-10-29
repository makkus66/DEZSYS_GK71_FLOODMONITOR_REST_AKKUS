package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationService {
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);
    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;
    private final Random random = new Random();

    public SimulationService(StationRepository stationRepository,
                             MeasurementRepository measurementRepository,
                             WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void simulate() {
        for (Station station : stationRepository.findAll()) {
            if (!station.isActive() || station.getStatus() == StationStatus.OFFLINE) {
                continue;
            }
            Measurement previous = measurementRepository.findLatest(station.getId()).orElse(null);
            Measurement next = nextMeasurement(station, previous);
            next.setWarningLevel(warningLevelService.calculate(station, next));
            measurementRepository.save(next);
            log.info("{}: waterLevel={} cm, flowRate={} m3/s, rainfall={} mm/h, battery={} %, status={}, warningLevel={}",
                    next.getStationId(), next.getWaterLevel(), next.getFlowRate(),
                    next.getRainfall(), next.getBatteryLevel(), next.getStatus(), next.getWarningLevel());
        }
    }

    private Measurement nextMeasurement(Station station, Measurement previous) {
        double prevLevel = previous == null ? station.getNormalWaterLevel() : previous.getWaterLevel();
        double prevRain = previous == null ? 0 : previous.getRainfall();
        double prevTemp = previous == null ? 12 : previous.getTemperature();
        int prevBattery = previous == null ? 60 + random.nextInt(41) : previous.getBatteryLevel();

        // Niederschlag: klingt langsam ab, gelegentlich Starkregen
        double rainfall = prevRain * 0.6 + random.nextDouble() * 3;
        if (random.nextDouble() < 0.1) {
            rainfall += 10 + random.nextDouble() * 20;
        }
        rainfall = clamp(rainfall, 0, 60);

        // Wasserstand: Regen erhöht, Pegel strebt zum Normalwert zurück, kleine Schwankung
        double waterLevel = prevLevel
                + rainfall * 0.8
                - (prevLevel - station.getNormalWaterLevel()) * 0.05
                + random.nextGaussian() * 2;
        waterLevel = Math.max(0, waterLevel);

        // Durchfluss steigt mit dem Wasserstand
        double flowRate = waterLevel * 2.5;

        double temperature = clamp(prevTemp + random.nextGaussian() * 0.3, -20, 40);

        // Akku: entlädt sich langsam, unter 10 % Wartung, in der Wartung wird geladen
        int battery = prevBattery;
        if (station.getStatus() == StationStatus.MAINTENANCE) {
            battery = Math.min(100, battery + 20);
            if (battery == 100) {
                station.setStatus(StationStatus.ONLINE);
            }
        } else {
            if (random.nextDouble() < 0.3) {
                battery = Math.max(0, battery - 1);
            }
            if (battery < 10) {
                station.setStatus(StationStatus.MAINTENANCE);
            }
        }

        return new Measurement(
                station.getId(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                round(waterLevel),
                round(flowRate),
                round(rainfall),
                round(temperature),
                battery,
                station.getStatus(),
                WarningLevel.UNKNOWN);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}