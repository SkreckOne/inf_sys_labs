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

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final MovieRepository movieRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final Validator validator;
    private final ApplicationEventPublisher eventPublisher;
    private final MinioService minioService;


    public void importMoviesFromJson(MultipartFile file) {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            minioService.uploadFile(objectName, file);

            InputStream stream = minioService.downloadFile(objectName);

            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            List<MovieDto> movieDtos = mapper.readValue(stream, new TypeReference<>() {});

            int importedCount = processMoviesTransactionally(movieDtos);

            importHistoryRepository.save(new ImportHistory("SUCCESS", importedCount, "Import successful", objectName));

            eventPublisher.publishEvent(new SseEvent("movies-imported", importedCount));

        } catch (Exception e) {

            minioService.deleteFile(objectName);

            importHistoryRepository.save(new ImportHistory("FAILURE", null, "Error: " + e.getMessage(), null));

            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int processMoviesTransactionally(List<MovieDto> movieDtos) {
        for (int i = 0; i < movieDtos.size(); i++) {
            MovieDto dto = movieDtos.get(i);
            Set<ConstraintViolation<MovieDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorDetails = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("; "));
                throw new RuntimeException("Validation failed at item " + i + ": " + errorDetails);
            }
        }

        List<Movie> movies = movieDtos.stream()
                .map(DtoMapper::toMovieEntity)
                .collect(Collectors.toList());

        movieRepository.saveAll(movies);

        return movies.size();
    }

    @Transactional(readOnly = true)
    public List<ImportHistory> getImportHistory() {
        return importHistoryRepository.findAll();
    }

    public InputStream getFileStream(String objectName) {
        return minioService.downloadFile(objectName);
    }
}