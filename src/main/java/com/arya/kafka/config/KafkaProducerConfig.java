package com.arya.kafka.config;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${boostrap.server: localhost:9092}")
    private String bootstrapServer;


    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //configure the following three settings for SSL Encryption
        //configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        //configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/var/private/ssl/kafka.client.truststore.jks");
        //configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234");

        // configure the following three settings for SSL Authentication
        //configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/var/private/ssl/kafka.client.keystore.jks");
        //configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234");
        //configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234");

        return new DefaultKafkaProducerFactory<>(configProps);
    }


    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        return new KafkaProducer<>((Map<String, Object>) producerFactory());
    }


//    private void testCallback(ProducerFactory<String, String> props) {
//        Producer<String, String> producer = new KafkaProducer<>((Map<String, Object>) props);
//        TestCallback callback = new TestCallback();
//
//        for (long i = 0; i < 100; i++) {
//            ProducerRecord<String, String> data = new ProducerRecord<>("test-topic", "key-" + i, "message-" + i);
//            producer.send(data, callback);
//        }
//        producer.close();
//    }
//
//    private static class TestCallback implements Callback {
//        @Override
//        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
//            if (e != null) {
//                System.out.println("Error while producing message to topic :"
//                        + recordMetadata);
//                e.printStackTrace();
//            } else {
//                String message = String.format("sent message to topic:%s partition:%s offset:%s ", recordMetadata.topic(),
//                        recordMetadata.partition(), recordMetadata.offset());
//                System.out.println(message);
//            }
//        }
//    }
}
