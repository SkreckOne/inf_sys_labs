package org.lab1.service;

import lombok.RequiredArgsConstructor;
import org.lab1.dto.CoordinatesDto;
import org.lab1.dto.MovieDto;
import org.lab1.dto.PersonDto;
import org.lab1.enums.MovieGenre;
import org.lab1.exception.MovieNotFoundException;
import org.lab1.mapper.DtoMapper;
import org.lab1.model.Coordinates;
import org.lab1.model.Movie;
import org.lab1.model.Person;
import org.lab1.repository.MovieRepository;
import org.lab1.repository.PersonRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final SseService sseService;
    private final ApplicationEventPublisher eventPublisher;

    public List<MovieDto> findAllMovies() {
        return DtoMapper.toMovieDtoList(movieRepository.findAll());
    }

    public MovieDto findMovieById(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with id " + id + " not found"));
        return DtoMapper.toMovieDto(movie);
    }

    @Transactional
    public MovieDto createMovie(MovieDto movieDto) {
        Movie movie = DtoMapper.toMovieEntity(movieDto);
        Movie savedMovie = movieRepository.save(movie);
        MovieDto resultDto = DtoMapper.toMovieDto(savedMovie);
        eventPublisher.publishEvent(new SseEvent("movie-created", resultDto));
        return resultDto;
    }

    @Transactional
    public MovieDto updateMovie(Integer id, MovieDto movieDto) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with id " + id + " not found"));

        existingMovie.setName(movieDto.getName());
        existingMovie.setOscarsCount(movieDto.getOscarsCount());
        existingMovie.setBudget(movieDto.getBudget());
        existingMovie.setTotalBoxOffice(movieDto.getTotalBoxOffice());
        existingMovie.setMpaaRating(movieDto.getMpaaRating());
        existingMovie.setLength(movieDto.getLength());
        existingMovie.setGoldenPalmCount(movieDto.getGoldenPalmCount());
        existingMovie.setUsaBoxOffice(movieDto.getUsaBoxOffice());
        existingMovie.setTagline(movieDto.getTagline());
        existingMovie.setGenre(movieDto.getGenre());

        updateCoordinatesFromDto(existingMovie.getCoordinates(), movieDto.getCoordinates());
        updatePersonFromDto(existingMovie.getDirector(), movieDto.getDirector());
        updatePersonFromDto(existingMovie.getOperator(), movieDto.getOperator());

        if (movieDto.getScreenwriter() != null) {
            if (existingMovie.getScreenwriter() == null) {
                existingMovie.setScreenwriter(new Person());
            }
            updatePersonFromDto(existingMovie.getScreenwriter(), movieDto.getScreenwriter());
        } else {
            existingMovie.setScreenwriter(null);
        }

        Movie updatedMovie = movieRepository.save(existingMovie);
        MovieDto resultDto = DtoMapper.toMovieDto(updatedMovie);
        eventPublisher.publishEvent(new SseEvent("movie-updated", resultDto));
        return resultDto;
    }

    private void updatePersonFromDto(Person person, PersonDto dto) {
        if (person == null || dto == null) return;
        person.setName(dto.getName());
        person.setEyeColor(dto.getEyeColor());
        person.setHairColor(dto.getHairColor());
        person.setBirthday(dto.getBirthday());
        person.setWeight(dto.getWeight());
    }


    private void updateCoordinatesFromDto(Coordinates entity, CoordinatesDto dto) {
        if (entity == null || dto == null) return;
        entity.setX(dto.getX());
        entity.setY(dto.getY());
    }

    @Transactional
    public void deleteMovie(Integer id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException("Movie with id " + id + " not found");
        }
        movieRepository.deleteById(id);
        eventPublisher.publishEvent(new SseEvent("movie-deleted", id));
    }

    @Transactional
    public void deleteMoviesByGenre(MovieGenre genre) {
        movieRepository.deleteAllByGenre(genre);
        eventPublisher.publishEvent(new SseEvent("movies-deleted-by-genre", genre.name()));
    }

    public Long getGoldenPalmSum() {
        return movieRepository.findAll().stream()
                .mapToLong(Movie::getGoldenPalmCount)
                .sum();
    }

    public List<MovieDto> findByTagline(String substring) {
        List<Movie> movies = movieRepository.findByTaglineContainingIgnoreCase(substring);
        return DtoMapper.toMovieDtoList(movies);
    }

    public List<PersonDto> findScreenwritersWithoutOscars() {
        List<Integer> idsWithOscars = personRepository.findScreenwriterIdsWithOscars();
        List<Person> allPersons = personRepository.findAll();

        List<Person> result = allPersons.stream()
                .filter(person -> !idsWithOscars.contains(person.getId()))
                .collect(Collectors.toList());

        return DtoMapper.toPersonDtoList(result);
    }

    @Transactional
    public void redistributeOscars(MovieGenre fromGenre, MovieGenre toGenre) {
        List<Movie> fromMovies = movieRepository.findByGenre(fromGenre);
        List<Movie> toMovies = movieRepository.findByGenre(toGenre);

        if (toMovies.isEmpty()) {
            return;
        }

        int totalOscars = fromMovies.stream()
                .filter(m -> m.getOscarsCount() != null)
                .mapToInt(Movie::getOscarsCount)
                .sum();

        if (totalOscars == 0) {
            return;
        }

        fromMovies.forEach(m -> m.setOscarsCount(null));
        movieRepository.saveAll(fromMovies);

        int oscarsPerMovie = totalOscars / toMovies.size();
        int remainder = totalOscars % toMovies.size();

        for (int i = 0; i < toMovies.size(); i++) {
            Movie movie = toMovies.get(i);
            int currentOscars = movie.getOscarsCount() == null ? 0 : movie.getOscarsCount();
            int oscarsToAdd = oscarsPerMovie + (i < remainder ? 1 : 0);
            movie.setOscarsCount(currentOscars + oscarsToAdd);
        }
        movieRepository.saveAll(toMovies);

        String eventData = "Oscars redistributed from " + fromGenre + " to " + toGenre;
        eventPublisher.publishEvent(new SseEvent("oscars-redistributed", eventData));
    }
}