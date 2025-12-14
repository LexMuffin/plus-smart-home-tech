package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.mapper.ProtoToAvroHubEventMapper;
import ru.yandex.practicum.mapper.ProtoToAvroSensorEventMapper;
import ru.yandex.practicum.mapper.SensorEventMapper;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@RequiredArgsConstructor
@Service
public class ProducerServiceimpl implements ProducerService {
    @Value("${collector.kafka.producer.topics.sensors-events}")
    private String sensorsEventsTopic;
    @Value("${collector.kafka.producer.topics.hubs-events}")
    private String hubsEventsTopic;

    private final KafkaClient kafkaClient;
    private final HubEventMapper hubEventMapper;
    private final SensorEventMapper sensorEventMapper;
    private final ProtoToAvroHubEventMapper protoToAvroHubEventMapper;
    private final ProtoToAvroSensorEventMapper protoToAvroSensorEventMapper;

    @Override
    public void processHubEvent(HubEvent hubEvent) {
        kafkaClient.send(
                hubsEventsTopic,
                hubEvent.getHubId(),
                hubEventMapper.toAvro(hubEvent)
        );
    }

    @Override
    public void processSensorEvent(SensorEvent sensorEvent) {
        kafkaClient.send(
                sensorsEventsTopic,
                sensorEvent.getHubId(),
                sensorEventMapper.toAvro(sensorEvent)
        );
    }

    @Override
    public void processSensorEvent(SensorEventProto sensorEventProto) {
        kafkaClient.send(
                sensorsEventsTopic,
                sensorEventProto.getHubId(),
                protoToAvroSensorEventMapper.toAvro(sensorEventProto)
        );
    }

    @Override
    public void processHubEvent(HubEventProto hubEventProto) {
        kafkaClient.send(
                hubsEventsTopic,
                hubEventProto.getHubId(),
                protoToAvroHubEventMapper.toAvro(hubEventProto)
        );
    }


}
