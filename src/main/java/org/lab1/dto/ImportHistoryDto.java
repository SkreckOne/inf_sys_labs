package org.lab1.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImportHistoryDto {
    private Long id;
    private LocalDateTime importDate;
    private String status;
    private Integer importedCount;
    private String details;
    private String objectName;
}