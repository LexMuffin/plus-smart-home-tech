package ru.yandex.practicum.service;

import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

public interface ProducerService {

    void processHubEvent(HubEvent hubEvent);

    void processSensorEvent(SensorEvent sensorEvent);
}
