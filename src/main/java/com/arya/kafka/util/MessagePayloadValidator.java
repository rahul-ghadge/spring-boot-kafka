package com.arya.kafka.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class providing common validation helpers for Kafka message payloads.
 *
 * <p>Centralises guard-clause logic to avoid duplication across producer services.
 *
 * @author rahul-ghadge
 */
@Slf4j
@UtilityClass
public class MessagePayloadValidator {

    private static final int MAX_MESSAGE_SIZE_BYTES = 1_048_576; // 1 MB

    /**
     * Validates that a string message is neither null, blank, nor exceeds the maximum size.
     *
     * @param message the message to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateStringMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message payload must not be null or blank");
        }
        if (message.getBytes().length > MAX_MESSAGE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Message payload exceeds maximum allowed size of " + MAX_MESSAGE_SIZE_BYTES + " bytes");
        }
    }

    /**
     * Validates that an object payload is non-null.
     *
     * @param payload the object to validate
     * @param label   a human-readable label used in the exception message
     * @throws IllegalArgumentException if the payload is null
     */
    public static void validateObjectPayload(Object payload, String label) {
        if (payload == null) {
            throw new IllegalArgumentException(label + " payload must not be null");
        }
    }
}
