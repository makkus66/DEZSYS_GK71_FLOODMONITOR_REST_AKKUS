# Cheatsheet – DEZSYS_GK71_FLOODMONITOR_REST

**Muhammed Akkus, 4CHIT** · Repo: https://github.com/makkus66/DEZSYS_GK71_FLOODMONITOR_REST_AKKUS

REST-Schnittstelle für ein Hochwasser-Frühwarnsystem: Spring Boot 3.2, Java 23, Gradle 8.10.2, JSON + XML, jQuery-Webclient.

---

## Starten

| Was | Befehl / URL |
|---|---|
| Server starten | `.\gradlew.bat bootRun` |
| Tests | `.\gradlew.bat test` |
| Webclient | http://localhost:8080/ |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |

Konfiguration (`application.properties`): `simulation.enabled=true`, `simulation.interval=10000`

---

## Architektur (Schichten)

**Controller → Service → Repository → Model**

| Schicht | Klassen | Aufgabe |
|---|---|---|
| Controller | StationController, AlertController | HTTP-Anfragen, keine Logik |
| Service | StationService, MeasurementService, WarningLevelService, SimulationService | Filter, Validierung, Statistik, Warnstufe, Simulation |
| Repository | StationRepository, MeasurementRepository | Daten im Arbeitsspeicher (max. 1000 Messwerte/Station) |
| Model | Station, Location, Measurement, Alert, Statistics, WarningLevel, StationStatus | Datenklassen, Enums |
| Exception | GlobalExceptionHandler, ErrorResponse | einheitliche Fehler |
| Client | static/index.html, css/style.css, js/app.js | Webclient |

---

## Stationen

| ID | Fluss | Normal | Warnung | Kritisch | Aktiv |
|---|---|---|---|---|---|
| ST-001 Wien | Donau | 280 | 450 | 600 | ja |
| ST-002 Linz | Donau | 300 | 550 | 700 | ja |
| ST-003 Innsbruck | Inn | 200 | 350 | 450 | ja |
| ST-004 Salzburg | Salzach | 250 | 450 | 600 | nein |

---

## Simulation (`SimulationService`, `@Scheduled`)

Jeder neue Wert baut auf dem **vorherigen** auf, nur für aktive, nicht-OFFLINE Stationen.

- **Regen** = 60 % vom vorherigen + 0–3 mm/h, 10 % Chance auf Starkregen (+10–30)
- **Wasserstand** = vorher + 0,8 × Regen − 5 % Abweichung zum Normalwert + Schwankung, nie negativ
- **Durchfluss** = 2,5 × Wasserstand
- **Akku** sinkt langsam, < 10 % → MAINTENANCE, lädt +20 %, bei 100 % → ONLINE

---

## Warnstufe (`WarningLevelService`, nur am Server)

| Bedingung | Stufe |
|---|---|
| keine Messung / älter als 5 min / ungültig | UNKNOWN |
| ≥ kritischer Wert | CRITICAL |
| ≥ Warnwert | WARNING |
| sonst | NORMAL |

---

## Endpunkte (`/api/v1`)

| Methode | Pfad | Filter |
|---|---|---|
| GET | `/stations` | river, isActive, warningLevel |
| GET | `/stations/{id}` | |
| GET | `/stations/{id}/measurements/latest` | |
| GET | `/stations/{id}/measurements` | from, to, limit (1–1000, Standard 100), warningLevel |
| GET | `/stations/{id}/statistics` | from, to |
| GET | `/alerts` | river, warningLevel |

---

## JSON / XML (Content Negotiation)

- Client schickt `Accept: application/json` oder `Accept: application/xml`
- Spring wählt den passenden **HttpMessageConverter** (Jackson JSON / Jackson XML durch `jackson-dataformat-xml`)
- Ohne Accept → **JSON** (Spring Boot stellt XML-Converter ans Ende)
- Kein passender Converter → **406 Not Acceptable**
- Test: `curl.exe -H "Accept: application/xml" http://localhost:8080/api/v1/stations/ST-001`

---

## Fehlerbehandlung (`@RestControllerAdvice`)

Einheitlich: `timestamp, status, error, message, path` – kein Stacktrace, JSON oder XML.

| Fall | Code |
|---|---|
| Station unbekannt / keine Messwerte | 404 |
| from nach to, falsches Datum, falsche Warnstufe, limit < 1 oder > 1000 | 400 |
| falsches Format (`text/plain`) | 406 |
| falsche Methode | 405 |
| sonstiger Fehler | 500 |

Bei 406 setze ich den Content-Type der Fehlerantwort fest auf JSON, sonst könnte Spring den Fehler selbst nicht ausgeben.

---

## Webclient (jQuery)

`$.ajax` mit `headers: { Accept: ... }` · JSON direkt, XML mit `$(xml).find("item")` auswerten · Filter Zeitraum / Warnstufe / Limit · `setInterval` alle 10 s · Ladeanzeige + roter Fehlerkasten · Farben: NORMAL grün, WARNING orange, CRITICAL rot, UNKNOWN grau

---

## Tests (JUnit 5 + MockMvc, 13 Tests)

- `@SpringBootTest(properties = "simulation.enabled=false")` → Simulation aus
- `@BeforeEach`: Speicher leeren, feste Messwerte anlegen → **nie zufällig rot**
- Warnstufen direkt am `WarningLevelService` getestet (449.9 → NORMAL, 450 → WARNING, 600 → CRITICAL)

---

## Mögliche Fragen im Gespräch

**Warum Service-Schicht?** Controller sollen nur Anfragen annehmen. Logik im Service ist testbar und wiederverwendbar.

**Wie kommt der vorherige Wert in die Simulation?** `measurementRepository.findLatest(id)` → wird an `nextMeasurement()` übergeben.

**Warum ist die Simulation in Tests aus?** Zufallswerte würden die Tests unvorhersehbar machen.

**Neuer Endpunkt – was ändern?** Controller-Methode + Mapping, Service-Logik, ggf. Repository/Model, Exception-Handler, OpenAPI-Annotationen, Tests.

**GET vs. POST vs. PUT vs. PATCH?** Lesen (sicher, idempotent) / anlegen (nicht idempotent) / ganz ersetzen (idempotent) / teilweise ändern.

**Fehler im Snippet `@GetMapping("/stations/add")`?** GET darf nichts ändern → richtig wäre `@PostMapping("/stations")` mit `@RequestBody`, Antwort **201 Created** + angelegte Station.

**OpenAPI vs. Swagger?** OpenAPI = Spezifikation (Format), Swagger = Tools (Swagger UI, Editor). springdoc erzeugt OpenAPI, Swagger UI zeigt es an.

**Authentifizierung?** Keine, weil die API nur lesende GET-Endpunkte für öffentliche Messdaten hat. Für schreibende Endpunkte: API-Key oder Spring Security (401/403).
