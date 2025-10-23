package org.lab1.mapper;

import org.lab1.dto.CoordinatesDto;
import org.lab1.dto.LocationDto;
import org.lab1.dto.MovieDto;
import org.lab1.dto.PersonDto;
import org.lab1.model.Coordinates;
import org.lab1.model.Location;
import org.lab1.model.Movie;
import org.lab1.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static Coordinates toCoordinatesEntity(CoordinatesDto dto) {
        if (dto == null) return null;
        Coordinates entity = new Coordinates();
        entity.setX(dto.getX());
        entity.setY(dto.getY());
        return entity;
    }

    public static Location toLocationEntity(LocationDto dto) {
        if (dto == null) return null;
        Location entity = new Location();
        entity.setX(dto.getX());
        entity.setY(dto.getY());
        entity.setZ(dto.getZ());
        entity.setName(dto.getName());
        return entity;
    }

    public static Person toPersonEntity(PersonDto dto) {
        if (dto == null) return null;
        Person entity = new Person();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setEyeColor(dto.getEyeColor());
        entity.setHairColor(dto.getHairColor());
        entity.setBirthday(dto.getBirthday());
        entity.setWeight(dto.getWeight());
        if (dto.getLocation() != null) {
            entity.setLocation(toLocationEntity(dto.getLocation()));
        }
        return entity;
    }

    public static Movie toMovieEntity(MovieDto dto) {
        if (dto == null) return null;
        Movie entity = new Movie();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCoordinates(toCoordinatesEntity(dto.getCoordinates()));
        entity.setOscarsCount(dto.getOscarsCount());
        entity.setBudget(dto.getBudget());
        entity.setTotalBoxOffice(dto.getTotalBoxOffice());
        entity.setMpaaRating(dto.getMpaaRating());
        entity.setDirector(toPersonEntity(dto.getDirector()));
        entity.setScreenwriter(toPersonEntity(dto.getScreenwriter()));
        entity.setOperator(toPersonEntity(dto.getOperator()));
        entity.setLength(dto.getLength());
        entity.setGoldenPalmCount(dto.getGoldenPalmCount());
        entity.setUsaBoxOffice(dto.getUsaBoxOffice());
        entity.setTagline(dto.getTagline());
        entity.setGenre(dto.getGenre());
        return entity;
    }



    public static CoordinatesDto toCoordinatesDto(Coordinates entity) {
        if (entity == null) return null;
        CoordinatesDto dto = new CoordinatesDto();
        dto.setX(entity.getX());
        dto.setY(entity.getY());
        return dto;
    }

    public static LocationDto toLocationDto(Location entity) {
        if (entity == null) return null;
        LocationDto dto = new LocationDto();
        dto.setX(entity.getX());
        dto.setY(entity.getY());
        dto.setZ(entity.getZ());
        dto.setName(entity.getName());
        return dto;
    }

    public static PersonDto toPersonDto(Person entity) {
        if (entity == null) return null;
        PersonDto dto = new PersonDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEyeColor(entity.getEyeColor());
        dto.setHairColor(entity.getHairColor());
        dto.setBirthday(entity.getBirthday());
        dto.setWeight(entity.getWeight());
        if (entity.getLocation() != null) {
            dto.setLocation(toLocationDto(entity.getLocation()));
        }
        return dto;
    }

    public static MovieDto toMovieDto(Movie entity) {
        if (entity == null) return null;
        MovieDto dto = new MovieDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCoordinates(toCoordinatesDto(entity.getCoordinates()));
        dto.setOscarsCount(entity.getOscarsCount());
        dto.setBudget(entity.getBudget());
        dto.setTotalBoxOffice(entity.getTotalBoxOffice());
        dto.setMpaaRating(entity.getMpaaRating());
        dto.setDirector(toPersonDto(entity.getDirector()));
        dto.setScreenwriter(toPersonDto(entity.getScreenwriter()));
        dto.setOperator(toPersonDto(entity.getOperator()));
        dto.setLength(entity.getLength());
        dto.setGoldenPalmCount(entity.getGoldenPalmCount());
        dto.setUsaBoxOffice(entity.getUsaBoxOffice());
        dto.setTagline(entity.getTagline());
        dto.setGenre(entity.getGenre());
        return dto;
    }

    public static List<MovieDto> toMovieDtoList(List<Movie> movies) {
        if (movies == null) return new ArrayList<>();
        return movies.stream().map(DtoMapper::toMovieDto).collect(Collectors.toList());
    }

    public static List<PersonDto> toPersonDtoList(List<Person> persons) {
        if (persons == null) return new ArrayList<>();
        return persons.stream().map(DtoMapper::toPersonDto).collect(Collectors.toList());
    }
}