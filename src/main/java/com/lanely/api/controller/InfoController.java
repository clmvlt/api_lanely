package com.lanely.api.controller;

import com.lanely.api.dto.ApiInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;

@RestController
@Tag(name = "Info", description = "Public information about the API")
public class InfoController {

    private final Environment environment;
    private final String version;

    public InfoController(Environment environment, @Value("${app.version:unknown}") String version) {
        this.environment = environment;
        this.version = version;
    }

    @GetMapping("/infos")
    @Operation(
            summary = "Get public API information",
            description = "Returns public, non-sensitive runtime information about the API: "
                    + "application name, version, active environment, health status, start instant and uptime."
    )
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "API information returned successfully",
                    content = @Content(schema = @Schema(implementation = ApiInfoDto.class))
            )
    )
    public ApiInfoDto getInfos() {
        long startMillis = ManagementFactory.getRuntimeMXBean().getStartTime();
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();

        String[] profiles = environment.getActiveProfiles();
        String env = profiles.length == 0 ? "default" : String.join(", ", profiles);
        String applicationName = environment.getProperty("spring.application.name", "api-lanely");

        return new ApiInfoDto(
                applicationName,
                version,
                env,
                "UP",
                Instant.ofEpochMilli(startMillis).toString(),
                formatUptime(uptimeMillis),
                uptimeMillis
        );
    }

    private String formatUptime(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (days > 0 || hours > 0) {
            builder.append(hours).append("h ");
        }
        if (days > 0 || hours > 0 || minutes > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");
        return builder.toString();
    }
}
