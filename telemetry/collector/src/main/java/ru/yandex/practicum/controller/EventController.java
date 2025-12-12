package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.ProducerService;

@RestController
@RequestMapping("/events")
@Slf4j
@AllArgsConstructor
public class EventController {

    private final ProducerService producerService;

    @PostMapping("/sensors")
    public void handleSensorEvent(@Valid @RequestBody SensorEvent sensorEvent) {
        producerService.processSensorEvent(sensorEvent);
    }

    @PostMapping("/hubs")
    public void handleHubEvent(@Valid @RequestBody HubEvent hubEvent) {
        producerService.processHubEvent(hubEvent);
    }
}
