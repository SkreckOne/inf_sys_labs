package org.lab1.repository;

import org.lab1.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    @Query("SELECT DISTINCT m.screenwriter.id FROM Movie m WHERE m.oscarsCount > 0 AND m.screenwriter IS NOT NULL")
    List<Integer> findScreenwriterIdsWithOscars();
}