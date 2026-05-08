package com.arya.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for publishing a batch of plain-text messages to the message topic in a single
 * HTTP call. The batch is limited to 100 messages per request to avoid overwhelming the broker.
 *
 * @author rahul-ghadge
 */
@Schema(description = "Request payload for batch-publishing multiple string messages")
public record BatchMessageRequest(

        @NotEmpty(message = "messages list must not be empty")
        @Size(max = 100, message = "Batch size must not exceed 100 messages per request")
        @Schema(description = "List of plain-text messages to publish", example = "[\"msg1\", \"msg2\"]")
        List<@NotBlank(message = "Individual message must not be blank") String> messages

) {}
