package org.lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinatesDto {
    @NotNull
    @Max(506)
    private Float x;

    @NotNull
    private int y;
}