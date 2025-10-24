package org.lab1.repository;

import org.lab1.enums.MovieGenre;
import org.lab1.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer>, JpaSpecificationExecutor<Movie> {
    void deleteAllByGenre(MovieGenre genre);
    List<Movie> findByTaglineContainingIgnoreCase(String substring);
    List<Movie> findByOscarsCount(Integer oscarsCount);
    List<Movie> findByGenre(MovieGenre genre);
}