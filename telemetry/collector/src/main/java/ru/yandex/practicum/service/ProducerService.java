package ru.yandex.practicum.service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface ProducerService {

    void processHubEvent(HubEventProto hubEventProto);

    void processSensorEvent(SensorEventProto sensorEventProto);


}
