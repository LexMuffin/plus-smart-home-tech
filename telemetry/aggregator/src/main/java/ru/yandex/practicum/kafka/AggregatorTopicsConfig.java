package ru.yandex.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("collector.kafka.topics")
public class AggregatorTopicsConfig {

    private String sensorsEvents = "telemetry.sensors.v1";
    private String hubsEvents = "telemetry.hubs.v1";
    private String snapshotsEvents = "telemetry.snapshots.v1";
}
