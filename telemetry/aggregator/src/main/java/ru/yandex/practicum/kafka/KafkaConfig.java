package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${collector.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    @ConfigurationProperties(prefix = "collector.kafka.producer")
    public Properties producerProperties() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootstrapServers);
        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        return props;
    }

    @Bean
    @ConfigurationProperties(prefix = "collector.kafka.consumer")
    public Properties consumerProperties() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "ru.yandex.practicum.deserializer.SensorEventDeserializer");
        props.setProperty("group.id", "aggregator-group");
        return props;
    }

    @Bean(destroyMethod = "close")
    public Producer<String, SpecificRecordBase> kafkaProducer(Properties producerProperties) {
        return new KafkaProducer<>(producerProperties);
    }

    @Bean(destroyMethod = "close")
    public Consumer<String, SpecificRecordBase> kafkaConsumer(Properties consumerProperties) {
        return new KafkaConsumer<>(consumerProperties);
    }

    @Bean
    public KafkaClient kafkaClient(
            Producer<String, SpecificRecordBase> kafkaProducer,
            Consumer<String, SpecificRecordBase> kafkaConsumer) {

        return new KafkaClient() {
            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                return kafkaProducer;
            }

            @Override
            public Consumer<String, SpecificRecordBase> getConsumer() {
                return kafkaConsumer;
            }

            @Override
            public void close() {
                try {
                    if (kafkaProducer != null) {
                        kafkaProducer.flush();
                        kafkaProducer.close(Duration.ofSeconds(10));
                    }
                    if (kafkaConsumer != null) {
                        kafkaConsumer.commitSync();
                        kafkaConsumer.close();
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки при закрытии
                }
            }
        };
    }
}

