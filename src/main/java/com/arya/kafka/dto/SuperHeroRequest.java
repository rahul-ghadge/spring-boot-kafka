package com.arya.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used to publish a SuperHero event to the Kafka topic.
 *
 * <p>Separating the API contract (this DTO) from the domain model ({@link com.arya.kafka.model.SuperHero})
 * allows the internal model to evolve independently of the public API surface.
 *
 * @author rahul-ghadge
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for publishing a SuperHero message to the Kafka topic")
public class SuperHeroRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Real name of the superhero", example = "Tony Stark", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Super name must not be blank")
    @Size(max = 100, message = "Super name must not exceed 100 characters")
    @Schema(description = "Superhero alias or code name", example = "Iron Man", requiredMode = Schema.RequiredMode.REQUIRED)
    private String superName;

    @NotBlank(message = "Profession must not be blank")
    @Schema(description = "Primary profession of the hero", example = "Business")
    private String profession;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must not exceed 150")
    @Schema(description = "Age of the superhero", example = "50")
    private int age;

    @Schema(description = "Whether the superhero can fly", example = "true")
    private boolean canFly;
}
