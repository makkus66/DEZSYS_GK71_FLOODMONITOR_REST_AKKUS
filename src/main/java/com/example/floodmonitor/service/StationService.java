package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.NotFoundException;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;

    public StationService(StationRepository stationRepository,
                          MeasurementRepository measurementRepository,
                          WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    public List<Station> findStations(String river, Boolean isActive, WarningLevel warningLevel) {
        return stationRepository.findAll().stream()
                .filter(s -> river == null || s.getRiver().equalsIgnoreCase(river))
                .filter(s -> isActive == null || s.isActive() == isActive)
                .filter(s -> warningLevel == null || currentWarningLevel(s) == warningLevel)
                .toList();
    }

    public Station getStation(String stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station " + stationId + " wurde nicht gefunden"));
    }

    public WarningLevel currentWarningLevel(Station station) {
        return warningLevelService.calculate(station,
                measurementRepository.findLatest(station.getId()).orElse(null));
    }
}