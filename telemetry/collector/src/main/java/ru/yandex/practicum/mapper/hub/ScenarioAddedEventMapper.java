package ru.yandex.practicum.mapper.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventMapper extends BaseHubEventMapper<ScenarioAddedEventAvro> {
    private final ScenarioConditionMapper conditionMapper;
    private final DeviceActionMapper actionMapper;

    @Override
    protected ScenarioAddedEventAvro mapToAvroPayload(HubEventProto event) {
        ScenarioAddedEventProto hubEvent = event.getScenarioAdded();
        log.info("Mapper bring event to {}, result: {}", ScenarioAddedEventProto.class.getSimpleName(), hubEvent);

        return ScenarioAddedEventAvro.newBuilder()
                .setName(hubEvent.getName())
                .setConditions(conditionMapper.mapToAvro(hubEvent.getConditionList()))
                .setActions(actionMapper.mapToAvro(hubEvent.getActionList()))
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getHubEventType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
