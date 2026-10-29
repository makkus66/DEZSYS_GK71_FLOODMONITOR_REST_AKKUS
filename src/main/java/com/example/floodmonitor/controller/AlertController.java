package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.ErrorResponse;
import com.example.floodmonitor.model.Alert;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/alerts",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "Warnungen", description = "Aktuelle Hochwasserwarnungen")
public class AlertController {

    private final MeasurementService measurementService;

    public AlertController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @Operation(summary = "Aktuelle Warnungen abrufen",
            description = "Liefert alle Stationen, deren aktueller Messwert WARNING oder CRITICAL ist.")
    @ApiResponse(responseCode = "200", description = "Liste der Warnungen (leer, wenn keine aktiv ist)")
    @ApiResponse(responseCode = "400", description = "Ungültige Warnstufe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public List<Alert> getAlerts(
            @Parameter(description = "Name des Flusses", example = "Donau")
            @RequestParam(required = false) String river,
            @Parameter(description = "Nur WARNING oder nur CRITICAL")
            @RequestParam(required = false) WarningLevel warningLevel) {
        return measurementService.getAlerts(river, warningLevel);
    }
}