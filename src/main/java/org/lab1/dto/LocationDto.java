package org.lab1.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LocationDto {
    @NotNull(message = "Location X cannot be null")
    private Float x;

    @NotNull(message = "Location Y cannot be null")
    private Long y;

    @NotNull(message = "Location Z cannot be null")
    private Double z;

    @NotBlank(message = "Location name cannot be empty")
    @Size(max = 475, message = "Location name length should not be greater than 475")
    private String name;
}