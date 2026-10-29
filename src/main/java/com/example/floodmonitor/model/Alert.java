package com.example.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "alert")
public class Alert {
    private String stationId;
    private String stationName;
    private String river;
    private Instant timestamp;
    private double waterLevel;
    private WarningLevel warningLevel;

    public Alert() {}

    public Alert(Station station, Measurement measurement, WarningLevel warningLevel) {
        this.stationId = station.getId();
        this.stationName = station.getName();
        this.river = station.getRiver();
        this.timestamp = measurement.getTimestamp();
        this.waterLevel = measurement.getWaterLevel();
        this.warningLevel = warningLevel;
    }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getRiver() { return river; }
    public void setRiver(String river) { this.river = river; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public double getWaterLevel() { return waterLevel; }
    public void setWaterLevel(double waterLevel) { this.waterLevel = waterLevel; }
    public WarningLevel getWarningLevel() { return warningLevel; }
    public void setWarningLevel(WarningLevel warningLevel) { this.warningLevel = warningLevel; }
}