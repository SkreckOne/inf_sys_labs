package org.lab1.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class ImportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime importDate;

    @Column(nullable = false)
    private String status;

    private Integer importedCount;

    @Column(length = 1024)
    private String details;

    public ImportHistory(String status, Integer importedCount, String details) {
        this.importDate = LocalDateTime.now();
        this.status = status;
        this.importedCount = importedCount;
        this.details = details;
    }
}