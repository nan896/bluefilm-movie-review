package com.bluefilm.moviehub.repository;

import com.bluefilm.moviehub.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
