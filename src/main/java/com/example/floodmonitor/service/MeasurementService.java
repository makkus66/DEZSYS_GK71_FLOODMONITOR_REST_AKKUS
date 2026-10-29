package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.BadRequestException;
import com.example.floodmonitor.exception.NotFoundException;
import com.example.floodmonitor.model.Alert;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Statistics;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MeasurementService {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 1000;

    private final StationService stationService;
    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;

    public MeasurementService(StationService stationService,
                              StationRepository stationRepository,
                              MeasurementRepository measurementRepository) {
        this.stationService = stationService;
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
    }

    public Measurement getLatest(String stationId) {
        stationService.getStation(stationId);
        return measurementRepository.findLatest(stationId)
                .orElseThrow(() -> new NotFoundException("Für Station " + stationId + " gibt es noch keine Messwerte"));
    }

    public List<Measurement> getMeasurements(String stationId, Instant from, Instant to,
                                             Integer limit, WarningLevel warningLevel) {
        stationService.getStation(stationId);
        validateRange(from, to);
        validateLimit(limit);
        int max = limit == null ? DEFAULT_LIMIT : limit;

        List<Measurement> filtered = filter(stationId, from, to).stream()
                .filter(m -> warningLevel == null || m.getWarningLevel() == warningLevel)
                .toList();

        // die neuesten "max" Messwerte zurückgeben
        return filtered.subList(Math.max(0, filtered.size() - max), filtered.size());
    }

    public Statistics getStatistics(String stationId, Instant from, Instant to) {
        stationService.getStation(stationId);
        validateRange(from, to);
        List<Measurement> list = filter(stationId, from, to);

        if (list.isEmpty()) {
            return new Statistics(stationId, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        double min = list.stream().mapToDouble(Measurement::getWaterLevel).min().orElse(0);
        double max = list.stream().mapToDouble(Measurement::getWaterLevel).max().orElse(0);
        double avgLevel = list.stream().mapToDouble(Measurement::getWaterLevel).average().orElse(0);
        double avgFlow = list.stream().mapToDouble(Measurement::getFlowRate).average().orElse(0);
        double rainSum = list.stream().mapToDouble(Measurement::getRainfall).sum();
        int warnings = (int) list.stream().filter(m -> m.getWarningLevel() == WarningLevel.WARNING).count();
        int criticals = (int) list.stream().filter(m -> m.getWarningLevel() == WarningLevel.CRITICAL).count();

        return new Statistics(stationId, min, max, round(avgLevel), round(avgFlow), round(rainSum),
                list.size(), warnings, criticals);
    }

    public List<Alert> getAlerts(String river, WarningLevel warningLevel) {
        List<Alert> alerts = new ArrayList<>();
        for (Station station : stationRepository.findAll()) {
            if (river != null && !station.getRiver().equalsIgnoreCase(river)) {
                continue;
            }
            Measurement latest = measurementRepository.findLatest(station.getId()).orElse(null);
            WarningLevel level = stationService.currentWarningLevel(station);
            if (latest == null || (level != WarningLevel.WARNING && level != WarningLevel.CRITICAL)) {
                continue;
            }
            if (warningLevel != null && level != warningLevel) {
                continue;
            }
            alerts.add(new Alert(station, latest, level));
        }
        return alerts;
    }

    private void validateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Ungültiger Zeitraum: from (" + from + ") liegt nach to (" + to + ")");
        }
    }

    private void validateLimit(Integer limit) {
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            throw new BadRequestException("limit muss zwischen 1 und " + MAX_LIMIT + " liegen");
        }
    }

    private List<Measurement> filter(String stationId, Instant from, Instant to) {
        return measurementRepository.findByStationId(stationId).stream()
                .filter(m -> from == null || !m.getTimestamp().isBefore(from))
                .filter(m -> to == null || !m.getTimestamp().isAfter(to))
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}