const API = "/api/v1";
const REFRESH_MS = 10000;

let format = "json";
let stationNames = {};
let timer = null;

// ---------- HTTP ----------

function accept() {
    return format === "xml" ? "application/xml" : "application/json";
}

function request(url) {
    return $.ajax({
        url: url,
        headers: { Accept: accept() },
        dataType: format === "xml" ? "xml" : "json"
    });
}

function errorMessage(xhr) {
    if (xhr.status === 0) {
        return "Server nicht erreichbar";
    }
    if (xhr.responseJSON && xhr.responseJSON.message) {
        return xhr.responseJSON.message;
    }
    if (xhr.responseXML) {
        const msg = $(xhr.responseXML).find("message").first().text();
        if (msg) return msg;
    }
    try {
        const json = JSON.parse(xhr.responseText);
        if (json.message) return json.message;
    } catch (e) { /* keine JSON-Antwort */ }
    return "Fehler " + xhr.status;
}

// ---------- JSON / XML auswerten ----------

function measurementFromXml(el) {
    const m = $(el);
    const t = name => m.children(name).text();
    return {
        stationId: t("stationId"),
        timestamp: t("timestamp"),
        waterLevel: Number(t("waterLevel")),
        flowRate: Number(t("flowRate")),
        rainfall: Number(t("rainfall")),
        temperature: Number(t("temperature")),
        batteryLevel: Number(t("batteryLevel")),
        status: t("status"),
        warningLevel: t("warningLevel")
    };
}

function parseStations(data) {
    if (format === "json") return data;
    return $(data).find("item").map(function () {
        const s = $(this);
        return {
            id: s.children("id").text(),
            name: s.children("name").text(),
            river: s.children("river").text()
        };
    }).get();
}

function parseMeasurements(data) {
    if (format === "json") return data;
    return $(data).find("item").map(function () {
        return measurementFromXml(this);
    }).get();
}

function parseMeasurement(data) {
    if (format === "json") return data;
    return measurementFromXml($(data).find("measurement").first());
}

// ---------- Anzeige ----------

function badge(level) {
    return '<span class="badge lvl-' + level + '">' + level + "</span>";
}

function formatTime(ts) {
    return new Date(ts).toLocaleString("de-AT");
}

function showError(text) {
    $("#error").text(text).show();
}

function clearError() {
    $("#error").hide().text("");
}

function setLoading(on) {
    $("#loading").toggle(on);
}

function showRaw(xhr) {
    const text = xhr.responseText || "";
    $("#raw").text(text.length > 3000 ? text.substring(0, 3000) + "\n..." : text);
}

// ---------- Laden ----------

function loadStations() {
    setLoading(true);
    return request(API + "/stations")
        .done(function (data) {
            const stations = parseStations(data);
            const select = $("#station").empty();
            stationNames = {};
            stations.forEach(function (s) {
                stationNames[s.id] = s.name;
                select.append($("<option>").val(s.id).text(s.id + " – " + s.name + " (" + s.river + ")"));
            });
        })
        .fail(function (xhr) {
            showError("Stationen konnten nicht geladen werden: " + errorMessage(xhr));
        })
        .always(function () {
            setLoading(false);
        });
}

function loadLatest() {
    const id = $("#station").val();
    if (!id) return;
    request(API + "/stations/" + id + "/measurements/latest")
        .done(function (data) {
            const m = parseMeasurement(data);
            $("#latest").html(
                "<div><b>Zeitpunkt:</b> " + formatTime(m.timestamp) + "</div>" +
                "<div><b>Wasserstand:</b> " + m.waterLevel + " cm</div>" +
                "<div><b>Durchfluss:</b> " + m.flowRate + " m³/s</div>" +
                "<div><b>Niederschlag:</b> " + m.rainfall + " mm/h</div>" +
                "<div><b>Temperatur:</b> " + m.temperature + " °C</div>" +
                "<div><b>Akku:</b> " + m.batteryLevel + " %</div>" +
                "<div><b>Status:</b> " + m.status + "</div>" +
                "<div><b>Warnstufe:</b> " + badge(m.warningLevel) + "</div>"
            );
        })
        .fail(function (xhr) {
            $("#latest").html('<span class="muted">' + errorMessage(xhr) + "</span>");
        });
}

function historyUrl() {
    const id = $("#station").val();
    const params = [];
    const from = $("#from").val();
    const to = $("#to").val();
    const level = $("#warningLevel").val();
    const limit = $("#limit").val();

    if (from) params.push("from=" + encodeURIComponent(new Date(from).toISOString()));
    if (to) params.push("to=" + encodeURIComponent(new Date(to).toISOString()));
    if (level) params.push("warningLevel=" + level);
    if (limit) params.push("limit=" + limit);

    return API + "/stations/" + id + "/measurements" + (params.length ? "?" + params.join("&") : "");
}

function loadHistory() {
    const id = $("#station").val();
    if (!id) return;
    setLoading(true);
    request(historyUrl())
        .done(function (data, status, xhr) {
            clearError();
            showRaw(xhr);
            const rows = parseMeasurements(data).slice().reverse();
            const body = $("#history tbody").empty();
            if (rows.length === 0) {
                body.append('<tr><td colspan="8" class="muted">Keine Messwerte für diese Filter</td></tr>');
            }
            rows.forEach(function (m) {
                body.append(
                    '<tr class="row-' + m.warningLevel + '">' +
                    "<td>" + (stationNames[m.stationId] || m.stationId) + "</td>" +
                    "<td>" + formatTime(m.timestamp) + "</td>" +
                    "<td>" + m.waterLevel + " cm</td>" +
                    "<td>" + m.flowRate + " m³/s</td>" +
                    "<td>" + m.rainfall + " mm/h</td>" +
                    "<td>" + m.batteryLevel + " %</td>" +
                    "<td>" + m.status + "</td>" +
                    "<td>" + badge(m.warningLevel) + "</td>" +
                    "</tr>"
                );
            });
            $("#updated").text("Zuletzt aktualisiert: " + new Date().toLocaleTimeString("de-AT") +
                " (" + format.toUpperCase() + ", " + rows.length + " Messwerte)");
        })
        .fail(function (xhr) {
            showRaw(xhr);
            showError(errorMessage(xhr));
        })
        .always(function () {
            setLoading(false);
        });
}

function loadAll() {
    loadLatest();
    loadHistory();
}

function startAutoRefresh() {
    stopAutoRefresh();
    timer = setInterval(loadAll, REFRESH_MS);
}

function stopAutoRefresh() {
    if (timer) clearInterval(timer);
    timer = null;
}

// ---------- Start ----------

$(function () {
    loadStations().done(loadAll);

    $("#station").on("change", loadAll);
    $("#filterForm").on("submit", function (e) {
        e.preventDefault();
        loadHistory();
    });
    $("#reset").on("click", function () {
        $("#from, #to, #warningLevel").val("");
        $("#limit").val("50");
        loadHistory();
    });
    $("input[name=format]").on("change", function () {
        format = $(this).val();
        const selected = $("#station").val();
        loadStations().done(function () {
            $("#station").val(selected);
            loadAll();
        });
    });
    $("#autoRefresh").on("change", function () {
        this.checked ? startAutoRefresh() : stopAutoRefresh();
    });

    startAutoRefresh();
});