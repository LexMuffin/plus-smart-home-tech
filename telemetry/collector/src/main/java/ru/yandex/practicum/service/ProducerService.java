package ru.yandex.practicum.service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

public interface ProducerService {

    void processHubEvent(HubEvent hubEvent);

    void processSensorEvent(SensorEvent sensorEvent);

    void processHubEvent(HubEventProto hubEventProto);

    void processSensorEvent(SensorEventProto sensorEventProto);


}
