package org.lab1.controller;

import lombok.RequiredArgsConstructor;
import org.lab1.dto.MovieDto;
import org.lab1.dto.PersonDto;
import org.lab1.enums.MovieGenre;
import org.lab1.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class SpecialOperationsController {

    private final MovieService movieService;

    @DeleteMapping("/genre/{genre}")
    public ResponseEntity<Void> deleteByGenre(@PathVariable MovieGenre genre) {
        movieService.deleteMoviesByGenre(genre);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/golden-palm-sum")
    public ResponseEntity<Map<String, Long>> getGoldenPalmSum() {
        long sum = movieService.getGoldenPalmSum();
        return ResponseEntity.ok(Map.of("totalGoldenPalms", sum));
    }

    @GetMapping("/tagline")
    public ResponseEntity<List<MovieDto>> findByTagline(@RequestParam String contains) {
        return ResponseEntity.ok(movieService.findByTagline(contains));
    }

    @GetMapping("/screenwriters-no-oscars")
    public ResponseEntity<List<PersonDto>> findScreenwritersWithoutOscars() {
        return ResponseEntity.ok(movieService.findScreenwritersWithoutOscars());
    }

    @PostMapping("/redistribute-oscars")
    public ResponseEntity<Void> redistributeOscars(@RequestParam MovieGenre from, @RequestParam MovieGenre to) {
        movieService.redistributeOscars(from, to);
        return ResponseEntity.ok().build();
    }
}