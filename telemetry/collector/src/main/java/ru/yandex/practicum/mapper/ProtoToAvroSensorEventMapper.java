package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class ProtoToAvroSensorEventMapper {
    public SensorEventAvro toAvro(SensorEventProto proto) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(proto.getId())
                .setHubId(proto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        proto.getTimestamp().getSeconds(),
                        proto.getTimestamp().getNanos()
                        ));

        SensorEventProto.PayloadCase payloadCase = proto.getPayloadCase();

        return switch (payloadCase) {
            case MOTION_SENSOR -> {
                MotionSensorProto event = proto.getMotionSensor();
                yield builder.setPayload(MotionSensorAvro.newBuilder()
                        .setLinkQuality(event.getLinkQuality())
                        .setMotion(event.getMotion())
                        .setVoltage(event.getVoltage())
                        .build()
                ).build();
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorProto event = proto.getTemperatureSensor();
                yield builder.setPayload(TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(event.getTemperatureC())
                        .setTemperatureF(event.getTemperatureF()).build()
                ).build();
            }
            case LIGHT_SENSOR -> {
                LightSensorProto event = proto.getLightSensor();
                yield builder.setPayload(LightSensorAvro.newBuilder()
                        .setLinkQuality(event.getLinkQuality())
                        .setLuminosity(event.getLuminosity())
                        .build()
                ).build();
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorProto event = proto.getClimateSensor();
                yield builder.setPayload(ClimateSensorAvro.newBuilder()
                        .setTemperatureC(event.getTemperatureC())
                        .setCo2Level(event.getCo2Level())
                        .setHumidity(event.getHumidity())
                        .build()
                ).build();
            }
            case SWITCH_SENSOR -> {
                SwitchSensorProto event = proto.getSwitchSensor();
                yield builder.setPayload(SwitchSensorAvro.newBuilder()
                        .setState(event.getState())
                        .build()
                ).build();
            }
            case PAYLOAD_NOT_SET -> builder.build();
        };
    }
}
