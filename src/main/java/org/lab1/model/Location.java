package org.lab1.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private Float x;

    @Column(nullable = false)
    private Long y;

    @Column(nullable = false)
    private Double z;

    @Column(nullable = false, length = 475)
    private String name;
}