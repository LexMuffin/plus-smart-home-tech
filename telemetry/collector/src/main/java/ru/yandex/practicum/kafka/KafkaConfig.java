package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.serializer.AvroSerializer;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${collector.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${collector.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${collector.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> initProducer(KafkaProperties properties) {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        return new KafkaProducer<>(config);
    }
}
