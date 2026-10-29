package com.example.floodmonitor.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "statistics")
public class Statistics {
    private String stationId;
    private double minWaterLevel;
    private double maxWaterLevel;
    private double avgWaterLevel;
    private double avgFlowRate;
    private double totalRainfall;
    private int measurementCount;
    private int warningCount;
    private int criticalCount;

    public Statistics() {}

    public Statistics(String stationId, double minWaterLevel, double maxWaterLevel, double avgWaterLevel,
                      double avgFlowRate, double totalRainfall, int measurementCount,
                      int warningCount, int criticalCount) {
        this.stationId = stationId;
        this.minWaterLevel = minWaterLevel;
        this.maxWaterLevel = maxWaterLevel;
        this.avgWaterLevel = avgWaterLevel;
        this.avgFlowRate = avgFlowRate;
        this.totalRainfall = totalRainfall;
        this.measurementCount = measurementCount;
        this.warningCount = warningCount;
        this.criticalCount = criticalCount;
    }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public double getMinWaterLevel() { return minWaterLevel; }
    public void setMinWaterLevel(double minWaterLevel) { this.minWaterLevel = minWaterLevel; }
    public double getMaxWaterLevel() { return maxWaterLevel; }
    public void setMaxWaterLevel(double maxWaterLevel) { this.maxWaterLevel = maxWaterLevel; }
    public double getAvgWaterLevel() { return avgWaterLevel; }
    public void setAvgWaterLevel(double avgWaterLevel) { this.avgWaterLevel = avgWaterLevel; }
    public double getAvgFlowRate() { return avgFlowRate; }
    public void setAvgFlowRate(double avgFlowRate) { this.avgFlowRate = avgFlowRate; }
    public double getTotalRainfall() { return totalRainfall; }
    public void setTotalRainfall(double totalRainfall) { this.totalRainfall = totalRainfall; }
    public int getMeasurementCount() { return measurementCount; }
    public void setMeasurementCount(int measurementCount) { this.measurementCount = measurementCount; }
    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }
}