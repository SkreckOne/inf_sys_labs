package org.lab1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.lab1.enums.Color;

import java.time.ZonedDateTime;

@Data
public class PersonDto {
    private Integer id;

    @NotBlank(message = "Person name cannot be empty")
    private String name;

    @NotNull(message = "Eye color cannot be null")
    private Color eyeColor;

    private Color hairColor;

    @Valid
    private LocationDto location;

    private ZonedDateTime birthday;

    @Positive(message = "Weight must be a positive value")
    private Float weight;
}