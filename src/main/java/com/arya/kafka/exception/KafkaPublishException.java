package com.arya.kafka.exception;

/**
 * Thrown when a Kafka producer fails to publish a message to a topic.
 *
 * @author rahul-ghadge
 */
public class KafkaPublishException extends RuntimeException {

    public KafkaPublishException(String message) {
        super(message);
    }

    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
