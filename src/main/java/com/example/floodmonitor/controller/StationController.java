package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class StationController {

    private final StationRepository stationRepository;

    public StationController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping("/stations")
    public List<Station> getStations() {
        return stationRepository.findAll();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return stationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}