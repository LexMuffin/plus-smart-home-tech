package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.mapper.hub.HubEventMapper;
import ru.yandex.practicum.mapper.sensor.SensorEventMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ProducerServiceimpl implements ProducerService {
    @Value("${collector.kafka.topics.sensors}")
    private String sensorsEventsTopic;
    @Value("${collector.kafka.topics.hubs}")
    private String hubsEventsTopic;

    private final KafkaClient kafkaClient;
    private final Map<SensorEventProto.PayloadCase, SensorEventMapper> sensorEventMappers;
    private final Map<HubEventProto.PayloadCase, HubEventMapper> hubEventMappers;

    public ProducerServiceimpl (
            KafkaClient kafkaClient,
            List<SensorEventMapper> sensorEventMappers,
            List<HubEventMapper> hubEventMappers
    ) {
        this.kafkaClient = kafkaClient;
        this.sensorEventMappers = sensorEventMappers.stream()
                .collect(Collectors.toMap(SensorEventMapper::getSensorEventType, Function.identity()));
        this.hubEventMappers = hubEventMappers.stream()
                .collect(Collectors.toMap(HubEventMapper::getHubEventType, Function.identity()));
    }

    @Override
    public void processSensorEvent(SensorEventProto sensorEventProto) {
        if (sensorEventMappers.containsKey(sensorEventProto.getPayloadCase())) {
            kafkaClient.send(
                    sensorsEventsTopic,
                    sensorEventProto.getHubId(),
                    sensorEventMappers.get(sensorEventProto.getPayloadCase()).mapToAvro(sensorEventProto)
            );
        } else {
            throw new IllegalArgumentException("Нет подходящего маппера");
        }
    }

    @Override
    public void processHubEvent(HubEventProto hubEventProto) {
        if (hubEventMappers.containsKey(hubEventProto.getPayloadCase())) {
            kafkaClient.send(
                    hubsEventsTopic,
                    hubEventProto.getHubId(),
                    hubEventMappers.get(hubEventProto.getPayloadCase()).mapToAvro(hubEventProto)
            );
        } else {
            throw new IllegalArgumentException("Нет подходящего маппера");
        }
    }
}
