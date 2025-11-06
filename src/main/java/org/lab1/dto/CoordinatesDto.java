package org.lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinatesDto {
    @NotNull()
    @Max(value = 506)
    private Float x;

    @NotNull()
    @Min(value = -307)
    private int y;
}