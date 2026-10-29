package com.example.floodmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI floodMonitorOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Flood Monitor REST API")
                .version("v1")
                .description("REST-Schnittstelle eines Hochwasser-Frühwarnsystems. "
                        + "Alle Endpunkte liefern standardmäßig JSON, mit Accept: application/xml auch XML. "
                        + "Fehler werden einheitlich mit timestamp, status, error, message und path zurückgegeben.\n\n"
                        + "Authentifizierung: Die API ist rein lesend (nur GET) und benötigt keine Authentifizierung."));
    }
}