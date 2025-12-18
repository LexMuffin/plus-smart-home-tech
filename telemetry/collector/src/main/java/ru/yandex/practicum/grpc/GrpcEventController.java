package ru.yandex.practicum.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.ProducerService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcEventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final ProducerService producerService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("Запрос SensorEventProto: {}", request);
        try {
            producerService.processSensorEvent(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            handleError(responseObserver, e, "collectSensorEvent");
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("Запрос HubEventProto: {}", request);
        try {
            producerService.processHubEvent(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            handleError(responseObserver, e, "collectHubEvent");
        }
    }

    private void handleError(StreamObserver<?> responseObserver, Exception e, String context) {
        log.error("Ошибка в {}: {}", context, e.getMessage(), e);
        responseObserver.onError(new StatusRuntimeException(
                Status.INTERNAL
                        .withDescription(e.getLocalizedMessage())
                        .withCause(e)
        ));
    }
}
