package ru.yandex.practicum.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroSerializer {

    private static final EncoderFactory encoderFactory = EncoderFactory.get();
    private static final ThreadLocal<BinaryEncoder> encoderThreadLocal = new ThreadLocal<>();

    public static <T extends SpecificRecordBase> byte[] serialize(T record) {
        if (record == null) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, encoderThreadLocal.get());

            DatumWriter<T> writer = new SpecificDatumWriter<>(record.getSchema());
            writer.write(record, encoder);
            encoder.flush();

            encoderThreadLocal.set(encoder);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException(
                    String.format("Ошибка сериализации Avro объекта типа [%s]",
                            record.getClass().getSimpleName()), ex);
        }
    }
}
