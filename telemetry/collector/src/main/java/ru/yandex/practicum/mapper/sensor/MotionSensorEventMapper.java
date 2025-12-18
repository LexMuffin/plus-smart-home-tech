package ru.yandex.practicum.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Slf4j
@Component
public class MotionSensorEventMapper extends BaseSensorEventMapper<MotionSensorAvro> {
    @Override
    protected MotionSensorAvro mapToAvroPayload(SensorEventProto event) {
        MotionSensorProto sensorEvent = event.getMotionSensorEvent();
        log.info("Mapper bring event to {}, result: {}", MotionSensorProto.class.getSimpleName(), sensorEvent);
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(sensorEvent.getLinkQuality())
                .setMotion(sensorEvent.getMotion())
                .setVoltage(sensorEvent.getVoltage())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }
}
