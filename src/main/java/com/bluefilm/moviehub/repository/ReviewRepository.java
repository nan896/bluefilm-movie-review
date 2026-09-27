package com.bluefilm.moviehub.repository;

import com.bluefilm.moviehub.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    List<Review> findTop8ByOrderByCreatedAtDesc();
}
