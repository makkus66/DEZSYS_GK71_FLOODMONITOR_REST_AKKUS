package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Alert;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final MeasurementService measurementService;

    public AlertController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @GetMapping
    public List<Alert> getAlerts(@RequestParam(required = false) String river,
                                 @RequestParam(required = false) WarningLevel warningLevel) {
        return measurementService.getAlerts(river, warningLevel);
    }
}