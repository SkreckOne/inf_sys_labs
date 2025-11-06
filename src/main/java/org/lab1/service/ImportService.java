package org.lab1.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.lab1.dto.MovieDto;
import org.lab1.mapper.DtoMapper;
import org.lab1.model.ImportHistory;
import org.lab1.model.Movie;
import org.lab1.repository.ImportHistoryRepository;
import org.lab1.repository.MovieRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final MovieRepository movieRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;


    public void importMoviesFromJson(MultipartFile file) {
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            List<MovieDto> movieDtos = mapper.readValue(file.getInputStream(), new TypeReference<>() {});

            int importedCount = processMoviesTransactionally(movieDtos);

            importHistoryRepository.save(new ImportHistory("SUCCESS", importedCount, "Import successful"));
            eventPublisher.publishEvent(new SseEvent("movies-imported", importedCount));

        } catch (Exception e) {
            importHistoryRepository.save(new ImportHistory("FAILURE", null, e.getMessage()));
            throw new RuntimeException("Failed to import movies: " + e.getMessage(), e);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int processMoviesTransactionally(List<MovieDto> movieDtos) {
        for (int i = 0; i < movieDtos.size(); i++) {
            final int movieIndex = i;
            MovieDto dto = movieDtos.get(i);
            Set<ConstraintViolation<MovieDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorDetails = violations.stream()
                        .map(v -> "Movie at index " + movieIndex + ", field '" + v.getPropertyPath() + "': " + v.getMessage())
                        .collect(Collectors.joining("; "));
                throw new RuntimeException("Validation failed: " + errorDetails);
            }
        }

        List<Movie> movies = movieDtos.stream().map(DtoMapper::toMovieEntity).collect(Collectors.toList());
        movieRepository.saveAll(movies);

        return movies.size();
    }

    @Transactional(readOnly = true)
    public List<ImportHistory> getImportHistory() {
        return importHistoryRepository.findAll();
    }
}