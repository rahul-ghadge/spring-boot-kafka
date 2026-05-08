package com.arya.kafka.controller;

import com.arya.kafka.dto.ApiResponse;
import com.arya.kafka.dto.BatchMessageRequest;
import com.arya.kafka.dto.NotificationRequest;
import com.arya.kafka.dto.SuperHeroRequest;
import com.arya.kafka.model.SuperHero;
import com.arya.kafka.service.ProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints to publish events to Kafka topics.
 *
 * <p>All responses are wrapped in a standardised {@link ApiResponse} envelope.
 * Request payloads are validated via Jakarta Bean Validation before being forwarded
 * to the {@link ProducerService}.
 *
 * <p>Available endpoints:
 * <ul>
 *   <li>{@code GET  /kafka/publish}       — publish a plain-text string message</li>
 *   <li>{@code POST /kafka/publish}        — publish a SuperHero JSON event</li>
 *   <li>{@code POST /kafka/publish/batch}  — publish a batch of string messages</li>
 *   <li>{@code POST /kafka/notify}         — publish a notification message</li>
 * </ul>
 *
 * @author rahul-ghadge
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Tag(name = "Kafka Publisher", description = "Endpoints for publishing messages and events to Kafka topics")
public class KafkaController {

    private final ProducerService producerService;

    // ─── String Message Endpoint ───────────────────────────────────────────────

    @Operation(
            summary     = "Publish a plain-text message",
            description = "Publishes a string message to the configured message topic."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202",
                    description = "Message accepted and enqueued for publishing"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid or blank message parameter")
    })
    @GetMapping("/publish")
    public ResponseEntity<ApiResponse<Void>> publishMessage(
            @Parameter(description = "The message payload to publish", example = "Hello Kafka!")
            @RequestParam @NotBlank(message = "Message must not be blank") String message) {

        log.info("REST request to publish string message");
        producerService.sendMessage(message);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Message published successfully"));
    }

    // ─── SuperHero JSON Endpoint ───────────────────────────────────────────────

    @Operation(
            summary     = "Publish a SuperHero event",
            description = "Serialises the SuperHero payload as JSON and publishes it to the superhero topic."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202",
                    description = "SuperHero event accepted and enqueued"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation failed for the request body")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content  = @Content(
                    mediaType = "application/json",
                    schema    = @Schema(implementation = SuperHeroRequest.class),
                    examples  = @ExampleObject(value = """
                            {
                              "name": "Tony Stark",
                              "superName": "Iron Man",
                              "profession": "Business",
                              "age": 50,
                              "canFly": true
                            }
                            """)
            )
    )
    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<Void>> publishSuperHero(
            @RequestBody @Valid SuperHeroRequest request) {

        log.info("REST request to publish SuperHero event: superName={}", request.getSuperName());

        SuperHero superHero = SuperHero.builder()
                .name(request.getName())
                .superName(request.getSuperName())
                .profession(request.getProfession())
                .age(request.getAge())
                .canFly(request.isCanFly())
                .build();

        producerService.sendSuperHeroMessage(superHero);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SuperHero event published successfully"));
    }

    // ─── Batch String Message Endpoint ────────────────────────────────────────

    @Operation(
            summary     = "Publish a batch of plain-text messages",
            description = "Iterates over the provided list and publishes each item to the message topic. "
                        + "Maximum 100 messages per request."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202",
                    description = "All messages accepted and enqueued"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation failed — list is empty, exceeds limit, or contains blank items")
    })
    @PostMapping("/publish/batch")
    public ResponseEntity<ApiResponse<Void>> publishBatch(
            @RequestBody @Valid BatchMessageRequest request) {

        log.info("REST request to publish batch of {} messages", request.messages().size());
        request.messages().forEach(producerService::sendMessage);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        String.format("Batch of %d messages published successfully", request.messages().size())));
    }

    // ─── Notification Endpoint ─────────────────────────────────────────────────

    @Operation(
            summary     = "Publish a notification",
            description = "Publishes a structured notification payload to the notification topic."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202",
                    description = "Notification accepted and enqueued"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Notification message is blank or exceeds character limit")
    })
    @PostMapping("/notify")
    public ResponseEntity<ApiResponse<Void>> publishNotification(
            @RequestBody @Valid NotificationRequest request) {

        log.info("REST request to publish notification");
        producerService.sendNotification(request.getMessage());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Notification published successfully"));
    }
}
