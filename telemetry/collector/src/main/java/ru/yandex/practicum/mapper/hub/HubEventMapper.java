package ru.yandex.practicum.mapper.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventMapper {
    HubEventProto.PayloadCase getHubEventType();

    HubEventAvro mapToAvro(HubEventProto event);
}
