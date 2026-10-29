package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class StationRepository {

    private final Map<String, Station> stations = new LinkedHashMap<>();

    public StationRepository() {
        save(new Station("ST-001", "Wien Reichsbrücke", "Donau", new Location(48.2289, 16.4102),
                280, 450, 600, true, StationStatus.ONLINE));
        save(new Station("ST-002", "Linz Nibelungenbrücke", "Donau", new Location(48.3076, 14.2858),
                300, 550, 700, true, StationStatus.ONLINE));
        save(new Station("ST-003", "Innsbruck Innbrücke", "Inn", new Location(47.2692, 11.3934),
                200, 350, 450, true, StationStatus.ONLINE));
        save(new Station("ST-004", "Salzburg Staatsbrücke", "Salzach", new Location(47.8017, 13.0446),
                250, 450, 600, false, StationStatus.OFFLINE));
    }

    public List<Station> findAll() {
        return new ArrayList<>(stations.values());
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public void save(Station station) {
        stations.put(station.getId(), station);
    }
}