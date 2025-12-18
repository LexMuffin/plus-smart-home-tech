package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Slf4j
public abstract class BaseSensorEventMapper<T extends SpecificRecordBase> implements SensorEventMapper {

    protected abstract T mapToAvroPayload(SensorEventProto event);

    @Override
    public SensorEventAvro mapToAvro(SensorEventProto event) {
        if (!event.getPayloadCase().equals(getSensorEventType())) {
            throw new IllegalArgumentException("Unknown type of event: " + event.getPayloadCase());
        }

        T payload = mapToAvroPayload(event);

        log.info("Create {}", SensorEventAvro.class.getSimpleName());
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
    }
}
