package org.lab1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.lab1.enums.MovieGenre;
import org.lab1.enums.MpaaRating;

@Data
public class MovieDto {
    private Integer id;

    @NotBlank(message = "Movie name cannot be empty")
    private String name;

    @NotNull(message = "Coordinates cannot be null")
    @Valid
    private CoordinatesDto coordinates;

    @Positive(message = "Oscars count must be a positive value")
    private Integer oscarsCount;

    @Positive(message = "Budget must be a positive value")
    private float budget;

    @NotNull(message = "Total box office cannot be null")
    @Positive(message = "Total box office must be a positive value")
    private Long totalBoxOffice;

    @NotNull(message = "MPAA rating cannot be null")
    private MpaaRating mpaaRating;

    @NotNull(message = "Director cannot be null")
    @Valid
    private PersonDto director;

    @Valid
    private PersonDto screenwriter;

    @NotNull(message = "Operator cannot be null")
    @Valid
    private PersonDto operator;

    @Positive(message = "Length must be a positive value")
    private Integer length;

    @NotNull(message = "Golden Palm count cannot be null")
    @Positive(message = "Golden Palm count must be a positive value")
    private Integer goldenPalmCount;

    @Positive(message = "USA box office must be a positive value")
    private Long usaBoxOffice;

    @NotBlank(message = "Tagline cannot be null")
    @Size(max = 168, message = "Tagline length must not exceed 168 characters")
    private String tagline;

    private MovieGenre genre;
}