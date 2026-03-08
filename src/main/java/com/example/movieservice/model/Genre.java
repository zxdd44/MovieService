package com.example.movieservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToMany;

import java.util.Set;

@Entity
@Table(name = "genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Жанр связан со многими фильмами (ManyToMany)
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies;

    public Genre() { }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
