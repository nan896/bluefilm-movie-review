package com.bluefilm.moviehub.web;

import com.bluefilm.moviehub.model.Movie;
import com.bluefilm.moviehub.model.Review;
import com.bluefilm.moviehub.model.User;
import com.bluefilm.moviehub.service.AuthService;
import com.bluefilm.moviehub.service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final AuthService authService;

    public MovieController(MovieService movieService, AuthService authService) {
        this.movieService = movieService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<Movie>> list(@RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(defaultValue = "全部") String type,
                                         @RequestParam(defaultValue = "rating") String sort) {
        return ApiResponse.ok(movieService.search(keyword, type, sort));
    }

    @GetMapping("/featured")
    public ApiResponse<Movie> featured() {
        return ApiResponse.ok(movieService.featured());
    }

    @GetMapping("/rankings")
    public ApiResponse<List<Movie>> rankings(@RequestParam(defaultValue = "8") int limit) {
        return ApiResponse.ok(movieService.rankings(limit));
    }

    @GetMapping("/recent-reviews")
    public ApiResponse<List<Review>> recentReviews() {
        return ApiResponse.ok(movieService.recentReviews());
    }

    @GetMapping("/{id}")
    public ApiResponse<Movie> detail(@PathVariable Long id) {
        return ApiResponse.ok(movieService.detail(id));
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<List<Review>> reviews(@PathVariable Long id) {
        return ApiResponse.ok(movieService.reviews(id));
    }

    @PostMapping("/{id}/reviews")
    public ApiResponse<Review> addReview(@PathVariable Long id,
                                         @RequestBody ReviewRequest request,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = authService.findByToken(authorization).orElse(null);
        Review review = movieService.addReview(id, user, request.nickname(), request.rating(), request.content());
        return ApiResponse.ok("评论发布成功", review);
    }

    public record ReviewRequest(String nickname, Integer rating, String content) {
    }
}
