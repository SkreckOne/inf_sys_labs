package org.lab1.controller;

import lombok.RequiredArgsConstructor;
import org.lab1.dto.ImportHistoryDto;
import org.lab1.mapper.DtoMapper;
import org.lab1.service.ImportService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Map<String, String>> importMovies(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            importService.importMoviesFromJson(file);
            return ResponseEntity.ok(Map.of("message", "File imported successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Import failed: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportHistoryDto>> getHistory() {
        return ResponseEntity.ok(DtoMapper.toImportHistoryDtoList(importService.getImportHistory()));
    }
}