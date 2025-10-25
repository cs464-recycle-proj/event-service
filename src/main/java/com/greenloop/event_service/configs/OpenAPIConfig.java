package com.greenloop.event_service.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        String serverUrl = "http://localhost:" + serverPort;

        return new OpenAPI()
            .info(new Info()
                .title("GreenLoop - Event API")
                .version("1.0.0")
                .description("This is a simple API for getting event data for our GreenLoop app.")
                .contact(new Contact()
                    .name("CS464 G1T7")
                    .url("Your Website")
                    .email("sonia.lim.2023@scis.smu.edu.sg"))
                .license(new License()
                    .name("Apache License 2.0")
                    .url("NA")))
            .addServersItem(new Server().url(serverUrl));
    }
}