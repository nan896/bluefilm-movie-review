package com.bluefilm.moviehub.web;

import com.bluefilm.moviehub.model.Movie;
import com.bluefilm.moviehub.service.AuthService;
import com.bluefilm.moviehub.service.MovieService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MovieService movieService;
    private final AuthService authService;

    public AdminController(MovieService movieService, AuthService authService) {
        this.movieService = movieService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ApiResponse<MovieService.Stats> stats(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return ApiResponse.ok(movieService.stats());
    }

    @GetMapping("/movies")
    public ApiResponse<List<Movie>> movies(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return ApiResponse.ok(movieService.search("", "全部", "latest"));
    }

    @PostMapping("/movies")
    public ApiResponse<Movie> create(@RequestBody MoviePayload payload,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return ApiResponse.ok("电影已添加", movieService.save(payload.toMovie(null)));
    }

    @PutMapping("/movies/{id}")
    public ApiResponse<Movie> update(@PathVariable Long id, @RequestBody MoviePayload payload,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        Movie existing = movieService.get(id);
        return ApiResponse.ok("电影已更新", movieService.save(payload.toMovie(existing)));
    }

    @DeleteMapping("/movies/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        movieService.delete(id);
        return ApiResponse.ok("电影已删除", null);
    }

    public record MoviePayload(String title, String originalTitle, Integer releaseYear, String type,
                               String region, String director, String actors, Integer duration,
                               String posterUrl, String summary, Double rating, Integer views,
                               Boolean featured) {

        Movie toMovie(Movie target) {
            Movie movie = target == null ? new Movie() : target;
            movie.setTitle(title);
            movie.setOriginalTitle(originalTitle);
            movie.setReleaseYear(releaseYear);
            movie.setType(type);
            movie.setRegion(region);
            movie.setDirector(director);
            movie.setActors(actors);
            movie.setDuration(duration);
            movie.setPosterUrl(posterUrl == null || posterUrl.isBlank()
                    ? "/assets/posters/poster-1.jpg" : posterUrl);
            movie.setSummary(summary);
            movie.setRating(rating);
            movie.setViews(views);
            movie.setFeatured(featured);
            return movie;
        }
    }
}
