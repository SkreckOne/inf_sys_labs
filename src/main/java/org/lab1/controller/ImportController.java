package org.lab1.controller;

import lombok.RequiredArgsConstructor;
import org.lab1.dto.ImportHistoryDto;
import org.lab1.mapper.DtoMapper;
import org.lab1.service.ImportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> importMovies(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "simulateError", defaultValue = "false") boolean simulateError
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            // Передаем флаг в сервис
            importService.importMoviesFromJson(file, simulateError);
            return ResponseEntity.ok(Map.of("message", "File imported successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Import failed: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportHistoryDto>> getHistory() {
        return ResponseEntity.ok(DtoMapper.toImportHistoryDtoList(importService.getImportHistory()));
    }

    @GetMapping("/file/{objectName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) {
        try {
            java.io.InputStream is = importService.getFileStream(objectName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(is));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}