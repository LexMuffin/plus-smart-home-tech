package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaClient implements AutoCloseable {
    private final KafkaProducer<String, SpecificRecordBase> producer;

    public void send(String topic, String key, SpecificRecordBase record) {
        producer.send(new ProducerRecord<>(topic, key, record), (recordMetadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке в Kafka", exception);
            }
        });
    }

    @Override
    public void close() throws Exception {
        producer.flush();
        producer.close();
    }
}
