package com.arya.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for publishing a notification message.
 *
 * @author rahul-ghadge
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for publishing a notification")
public class NotificationRequest {

    @NotBlank(message = "Notification message must not be blank")
    @Size(max = 1000, message = "Notification must not exceed 1000 characters")
    @Schema(description = "The notification content", example = "system alert: disk usage at 90%")
    private String message;
}
