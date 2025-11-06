package org.lab1.repository;

import org.lab1.model.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
}