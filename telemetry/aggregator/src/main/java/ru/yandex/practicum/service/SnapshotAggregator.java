package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SnapshotAggregator {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        if (event == null || event.getHubId() == null || event.getId() == null) {
            return Optional.empty();
        }
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, id -> SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(new HashMap<>())
                .build()
        );

        Map<String, SensorStateAvro> sensorStates = new HashMap<>(snapshot.getSensorsState());
        SensorStateAvro oldState = sensorStates.get(sensorId);

        Instant newTimestamp = event.getTimestamp();
        if (oldState != null) {
            Instant oldTimestamp = oldState.getTimestamp();

            // Проверяем, если старое событие более новое или данные не изменились
            if (oldTimestamp.isAfter(newTimestamp) ||
                    oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorStates.put(sensorId, newState);

        // Обновляем снапшот (создаём копию с обновлёнными данными)
        SensorsSnapshotAvro updateSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorStates)
                .build();

        snapshots.put(hubId, updateSnapshot);

        return Optional.of(updateSnapshot);
    }
}
