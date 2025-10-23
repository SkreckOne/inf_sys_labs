package org.lab1.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private Float x;

    @Column(nullable = false)
    private int y;
}