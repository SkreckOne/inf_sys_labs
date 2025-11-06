package org.lab1.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.lab1.enums.MovieGenre;
import org.lab1.enums.MpaaRating;
import java.time.LocalDate;

@Data
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Version
    private Integer version;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "coordinates_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Coordinates coordinates;

    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;

    private Integer oscarsCount;

    @Column(nullable = false)
    private float budget;

    @Column(nullable = false)
    private Long totalBoxOffice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MpaaRating mpaaRating;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "director_id", nullable = false)
    private Person director;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "operator_id", nullable = false)
    private Person operator;

    private Integer length;

    @Column(nullable = false)
    private Integer goldenPalmCount;

    private Long usaBoxOffice;

    @Column(nullable = false, length = 168)
    private String tagline;

    @Enumerated(EnumType.STRING)
    private MovieGenre genre;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }
}