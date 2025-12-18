package ru.yandex.practicum.mapper.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Slf4j
@Component
public class DeviceRemovedEventMapper extends BaseHubEventMapper<DeviceRemovedEventAvro> {

    @Override
    protected DeviceRemovedEventAvro mapToAvroPayload(HubEventProto event) {
        DeviceRemovedEventProto hubEvent = event.getDeviceRemoved();
        log.info("Mapper bring event to {}, result: {}", DeviceRemovedEventProto.class.getSimpleName(), hubEvent);

        return DeviceRemovedEventAvro.newBuilder()
                .setId(hubEvent.getId())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getHubEventType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}
