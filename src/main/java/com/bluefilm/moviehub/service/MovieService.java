package com.bluefilm.moviehub.service;

import com.bluefilm.moviehub.model.Movie;
import com.bluefilm.moviehub.model.Review;
import com.bluefilm.moviehub.model.User;
import com.bluefilm.moviehub.repository.MovieRepository;
import com.bluefilm.moviehub.repository.ReviewRepository;
import com.bluefilm.moviehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public MovieService(MovieRepository movieRepository, ReviewRepository reviewRepository,
                        UserRepository userRepository) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> search(String keyword, String type, String sort) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedType = type == null ? "" : type.trim();
        Comparator<Movie> comparator = switch (sort == null ? "rating" : sort) {
            case "popular" -> Comparator.comparing(Movie::getViews, Comparator.nullsLast(Integer::compareTo)).reversed();
            case "latest" -> Comparator.comparing(Movie::getReleaseYear, Comparator.nullsLast(Integer::compareTo)).reversed();
            default -> Comparator.comparing(Movie::getRating, Comparator.nullsLast(Double::compareTo)).reversed();
        };

        return movieRepository.findAll().stream()
                .filter(movie -> normalizedKeyword.isBlank()
                        || contains(movie.getTitle(), normalizedKeyword)
                        || contains(movie.getOriginalTitle(), normalizedKeyword)
                        || contains(movie.getDirector(), normalizedKeyword)
                        || contains(movie.getActors(), normalizedKeyword))
                .filter(movie -> normalizedType.isBlank() || normalizedType.equals("全部")
                        || (movie.getType() != null && movie.getType().contains(normalizedType)))
                .sorted(comparator.thenComparing(Movie::getId, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional
    public Movie detail(Long id) {
        Movie movie = get(id);
        movie.setViews((movie.getViews() == null ? 0 : movie.getViews()) + 1);
        return movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public Movie featured() {
        return movieRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getFeatured()))
                .findFirst()
                .orElseGet(() -> movieRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("还没有电影数据")));
    }

    @Transactional(readOnly = true)
    public List<Movie> rankings(int limit) {
        return movieRepository.findAll().stream()
                .sorted(Comparator.comparing(Movie::getRating, Comparator.nullsLast(Double::compareTo)).reversed())
                .limit(Math.max(1, Math.min(limit, 20)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Movie get(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("电影不存在"));
    }

    @Transactional(readOnly = true)
    public List<Review> reviews(Long movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
    }

    @Transactional(readOnly = true)
    public List<Review> recentReviews() {
        return reviewRepository.findTop8ByOrderByCreatedAtDesc();
    }

    @Transactional
    public Review addReview(Long movieId, User user, String nickname, Integer rating, String content) {
        Movie movie = get(movieId);
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("请选择 1 到 5 星评分");
        }
        if (content == null || content.trim().length() < 5) {
            throw new IllegalArgumentException("评论至少需要 5 个字符");
        }

        Review review = new Review();
        review.setMovieId(movieId);
        review.setUserId(user == null ? null : user.getId());
        review.setNickname(user != null ? user.getNickname()
                : (nickname == null || nickname.isBlank() ? "匿名影迷" : nickname.trim()));
        review.setAvatarUrl(user != null ? user.getAvatarUrl() : "/assets/avatars/avatar-6.jpg");
        review.setRating(rating);
        review.setContent(content.trim());
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        List<Review> allReviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
        double averageStars = allReviews.stream().mapToInt(Review::getRating).average().orElse(0);
        double score = BigDecimal.valueOf(averageStars * 2.0)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        movie.setRating(score);
        movie.setRatingCount(allReviews.size());
        movieRepository.save(movie);
        return saved;
    }

    @Transactional
    public Movie save(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("电影名称不能为空");
        }
        movie.setTitle(movie.getTitle().trim());
        movie.setType(movie.getType() == null || movie.getType().isBlank() ? "剧情" : movie.getType().trim());
        movie.setRating(movie.getRating() == null ? 0.0 : movie.getRating());
        movie.setRatingCount(movie.getRatingCount() == null ? 0 : movie.getRatingCount());
        movie.setViews(movie.getViews() == null ? 0 : movie.getViews());
        movie.setFeatured(Boolean.TRUE.equals(movie.getFeatured()));
        return movieRepository.save(movie);
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.deleteById(id);
        reviewRepository.findByMovieIdOrderByCreatedAtDesc(id).forEach(reviewRepository::delete);
    }

    @Transactional(readOnly = true)
    public Stats stats() {
        List<Movie> movies = movieRepository.findAll();
        double average = movies.stream()
                .map(Movie::getRating)
                .filter(value -> value != null && value > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        return new Stats(movies.size(), reviewRepository.count(), userRepository.count(),
                BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue());
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    public record Stats(long movies, long reviews, long users, double averageRating) {
    }
}
