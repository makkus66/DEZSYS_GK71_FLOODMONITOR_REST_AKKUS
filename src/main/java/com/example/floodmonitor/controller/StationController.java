package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.ErrorResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.Statistics;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/stations",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Tag(name = "Stationen", description = "Messstationen, Messwerte und Statistik")
public class StationController {

    private final StationService stationService;
    private final MeasurementService measurementService;

    public StationController(StationService stationService, MeasurementService measurementService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
    }

    @Operation(summary = "Alle Messstationen abrufen",
            description = "Optional nach Fluss, Aktivität und aktueller Warnstufe filterbar.")
    @ApiResponse(responseCode = "200", description = "Liste der Stationen")
    @ApiResponse(responseCode = "400", description = "Ungültiger Filterwert",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "406", description = "Format nicht unterstützt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public List<Station> getStations(
            @Parameter(description = "Name des Flusses", example = "Donau")
            @RequestParam(required = false) String river,
            @Parameter(description = "Nur aktive (true) oder inaktive (false) Stationen")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Aktuelle Warnstufe der Station")
            @RequestParam(required = false) WarningLevel warningLevel) {
        return stationService.findStations(river, isActive, warningLevel);
    }

    @Operation(summary = "Einzelne Station abrufen")
    @ApiResponse(responseCode = "200", description = "Die Station")
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{stationId}")
    public Station getStation(
            @Parameter(description = "ID der Station", example = "ST-001") @PathVariable String stationId) {
        return stationService.getStation(stationId);
    }

    @Operation(summary = "Aktuellsten Messwert einer Station abrufen")
    @ApiResponse(responseCode = "200", description = "Der neueste Messwert")
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden oder noch keine Messwerte",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{stationId}/measurements/latest")
    public Measurement getLatestMeasurement(
            @Parameter(description = "ID der Station", example = "ST-001") @PathVariable String stationId) {
        return measurementService.getLatest(stationId);
    }

    @Operation(summary = "Historische Messwerte abrufen",
            description = "Liefert die neuesten Messwerte im gewählten Zeitraum, maximal 'limit' Stück (Standard 100, höchstens 1000).")
    @ApiResponse(responseCode = "200", description = "Liste der Messwerte")
    @ApiResponse(responseCode = "400", description = "Ungültiger Zeitraum, Datumsformat, limit oder Warnstufe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{stationId}/measurements")
    public List<Measurement> getMeasurements(
            @Parameter(description = "ID der Station", example = "ST-001") @PathVariable String stationId,
            @Parameter(description = "Startzeitpunkt (ISO-8601)", example = "2026-05-01T08:00:00Z")
            @RequestParam(required = false) Instant from,
            @Parameter(description = "Endzeitpunkt (ISO-8601)", example = "2026-05-01T12:00:00Z")
            @RequestParam(required = false) Instant to,
            @Parameter(description = "Maximale Anzahl (1 bis 1000)", example = "100")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Nur Messwerte mit dieser Warnstufe")
            @RequestParam(required = false) WarningLevel warningLevel) {
        return measurementService.getMeasurements(stationId, from, to, limit, warningLevel);
    }

    @Operation(summary = "Statistik einer Station abrufen",
            description = "Minimum, Maximum und Durchschnitt des Wasserstands, durchschnittlicher Durchfluss, "
                    + "Niederschlagssumme sowie Anzahl der Messungen, Warnungen und kritischen Warnungen.")
    @ApiResponse(responseCode = "200", description = "Statistik")
    @ApiResponse(responseCode = "400", description = "Ungültiger Zeitraum oder Datumsformat",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{stationId}/statistics")
    public Statistics getStatistics(
            @Parameter(description = "ID der Station", example = "ST-001") @PathVariable String stationId,
            @Parameter(description = "Startzeitpunkt (ISO-8601)") @RequestParam(required = false) Instant from,
            @Parameter(description = "Endzeitpunkt (ISO-8601)") @RequestParam(required = false) Instant to) {
        return measurementService.getStatistics(stationId, from, to);
    }
}