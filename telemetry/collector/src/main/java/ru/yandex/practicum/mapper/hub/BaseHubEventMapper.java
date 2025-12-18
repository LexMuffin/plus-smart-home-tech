package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Slf4j
public abstract class BaseHubEventMapper<T extends SpecificRecordBase> implements HubEventMapper {

    protected abstract T mapToAvroPayload(HubEventProto event);

    @Override
    public HubEventAvro mapToAvro(HubEventProto event) {
        if (!event.getPayloadCase().equals(getHubEventType())) {
            throw new IllegalArgumentException("Unknown type of event: " + event.getPayloadCase());
        }

        T payload = mapToAvroPayload(event);

        log.info("Create {}", HubEventAvro.class.getSimpleName());
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
    }
}
