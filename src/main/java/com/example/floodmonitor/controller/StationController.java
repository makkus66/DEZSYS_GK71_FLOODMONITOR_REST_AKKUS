package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Statistics;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;
    private final MeasurementService measurementService;

    public StationController(StationService stationService, MeasurementService measurementService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
    }

    @GetMapping
    public List<Station> getStations(@RequestParam(required = false) String river,
                                     @RequestParam(required = false) Boolean isActive,
                                     @RequestParam(required = false) WarningLevel warningLevel) {
        return stationService.findStations(river, isActive, warningLevel);
    }

    @GetMapping("/{stationId}")
    public Station getStation(@PathVariable String stationId) {
        return stationService.getStation(stationId);
    }

    @GetMapping("/{stationId}/measurements/latest")
    public Measurement getLatestMeasurement(@PathVariable String stationId) {
        return measurementService.getLatest(stationId);
    }

    @GetMapping("/{stationId}/measurements")
    public List<Measurement> getMeasurements(@PathVariable String stationId,
                                             @RequestParam(required = false) Instant from,
                                             @RequestParam(required = false) Instant to,
                                             @RequestParam(required = false) Integer limit,
                                             @RequestParam(required = false) WarningLevel warningLevel) {
        return measurementService.getMeasurements(stationId, from, to, limit, warningLevel);
    }

    @GetMapping("/{stationId}/statistics")
    public Statistics getStatistics(@PathVariable String stationId,
                                    @RequestParam(required = false) Instant from,
                                    @RequestParam(required = false) Instant to) {
        return measurementService.getStatistics(stationId, from, to);
    }
}