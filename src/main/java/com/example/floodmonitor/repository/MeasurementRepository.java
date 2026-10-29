package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class MeasurementRepository {

    private static final int MAX_PER_STATION = 1000;

    private final Map<String, List<Measurement>> measurements = new ConcurrentHashMap<>();

    public void save(Measurement measurement) {
        List<Measurement> list = measurements.computeIfAbsent(
                measurement.getStationId(), id -> new CopyOnWriteArrayList<>());
        list.add(measurement);
        if (list.size() > MAX_PER_STATION) {
            list.remove(0);
        }
    }

    public List<Measurement> findByStationId(String stationId) {
        return measurements.getOrDefault(stationId, List.of());
    }

    public Optional<Measurement> findLatest(String stationId) {
        List<Measurement> list = findByStationId(stationId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }
    public void deleteAll() {
        measurements.clear();
    }
}