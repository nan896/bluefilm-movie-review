package com.bluefilm.moviehub;

import com.bluefilm.moviehub.model.Movie;
import com.bluefilm.moviehub.model.Review;
import com.bluefilm.moviehub.model.User;
import com.bluefilm.moviehub.repository.MovieRepository;
import com.bluefilm.moviehub.repository.ReviewRepository;
import com.bluefilm.moviehub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;

    public DataSeeder(UserRepository userRepository, MovieRepository movieRepository,
                      ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedMoviesAndReviews();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(user("admin", "admin123", "平台管理员", "ADMIN", 1));
        userRepository.save(user("demo", "demo123", "蓝影体验官", "USER", 2));
    }

    private void seedMoviesAndReviews() {
        if (movieRepository.count() > 0) {
            return;
        }

        List<Movie> movies = List.of(
                movie("流浪地球", "The Wandering Earth", 2019, "科幻 / 灾难", "中国大陆",
                        "郭帆", "吴京 / 屈楚萧 / 李光洁", 125, 1, true,
                        "太阳即将毁灭，人类在地球表面建造巨大推进器，带着家园寻找新的恒星。"),
                movie("我不是药神", "Dying to Survive", 2018, "剧情 / 喜剧", "中国大陆",
                        "文牧野", "徐峥 / 王传君 / 周一围", 117, 2, true,
                        "一位普通中年男人偶然成为仿制药代理商，在利益、法与情之间不断作出选择。"),
                movie("千与千寻", "Spirited Away", 2001, "动画 / 奇幻", "日本",
                        "宫崎骏", "柊瑠美 / 入野自由 / 夏木真理", 125, 3, true,
                        "少女千寻误入神灵世界，为了救回父母，她必须学会独立、勇敢并记住自己的名字。"),
                movie("星际穿越", "Interstellar", 2014, "科幻 / 冒险", "美国 / 英国",
                        "克里斯托弗·诺兰", "马修·麦康纳 / 安妮·海瑟薇", 169, 4, false,
                        "地球环境恶化，一群宇航员穿越虫洞寻找适合人类生存的新家园。"),
                movie("霸王别姬", "Farewell My Concubine", 1993, "剧情 / 爱情", "中国大陆 / 中国香港",
                        "陈凯歌", "张国荣 / 张丰毅 / 巩俐", 171, 5, false,
                        "两位京剧演员跨越时代变迁的人生，与舞台上的悲欢离合交织在一起。"),
                movie("隐入尘烟", "Return to Dust", 2022, "剧情 / 爱情", "中国大陆",
                        "李睿珺", "武仁林 / 海清 / 杨光锐", 133, 6, false,
                        "西北农村，两个被家庭边缘化的人相互扶持，在土地和四季里建立朴素的生活。"),
                movie("肖申克的救赎", "The Shawshank Redemption", 1994, "剧情 / 犯罪", "美国",
                        "弗兰克·德拉邦特", "蒂姆·罗宾斯 / 摩根·弗里曼", 142, 1, false,
                        "银行家安迪蒙冤入狱，他用多年时间守住希望，也悄悄为自己铺出一条自由之路。")
        );
        movieRepository.saveAll(movies);

        String[] reviewContents = {
                "节奏很稳，人物和情绪都落得很实，看完之后还会想很久。",
                "画面和音乐很有代入感，几个关键场景的处理尤其打动我。",
                "故事不急着给答案，但细节足够丰富，是值得重看的作品。",
                "它不是靠大场面撑起来，真正有力量的是人物之间的选择。",
                "叙事清晰，情感克制，放在今天看依然不过时。",
                "有几处转折很惊喜，结尾也留了足够的余味。",
                "适合安静地看完，然后再和朋友一起聊聊里面的细节。"
        };
        String[] nicknames = {"银幕散步者", "蓝色放映厅", "周末看片人", "光影笔记", "豆瓣风格新用户"};
        String[] avatars = {
                "/assets/avatars/avatar-1.jpg", "/assets/avatars/avatar-2.jpg",
                "/assets/avatars/avatar-3.jpg", "/assets/avatars/avatar-4.jpg",
                "/assets/avatars/avatar-5.jpg"
        };

        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            for (int j = 0; j < 3; j++) {
                Review review = new Review();
                review.setMovieId(movie.getId());
                review.setUserId(null);
                review.setNickname(nicknames[(i + j) % nicknames.length]);
                review.setAvatarUrl(avatars[(i + j) % avatars.length]);
                review.setRating(j == 1 ? 4 : 5);
                review.setContent(reviewContents[(i + j) % reviewContents.length]);
                review.setCreatedAt(LocalDateTime.now().minusDays(i * 3L + j + 1L));
                reviewRepository.save(review);
            }
            double score = (i % 3 == 0) ? 9.2 : (i % 3 == 1 ? 8.8 : 9.0);
            movie.setRating(score);
            movie.setRatingCount(3);
            movieRepository.save(movie);
        }
    }

    private User user(String username, String password, String nickname, String role, int avatar) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setRole(role);
        user.setAvatarUrl("/assets/avatars/avatar-" + avatar + ".jpg");
        return user;
    }

    private Movie movie(String title, String originalTitle, int year, String type, String region,
                        String director, String actors, int duration, int poster, boolean featured,
                        String summary) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setOriginalTitle(originalTitle);
        movie.setReleaseYear(year);
        movie.setType(type);
        movie.setRegion(region);
        movie.setDirector(director);
        movie.setActors(actors);
        movie.setDuration(duration);
        movie.setPosterUrl("/assets/posters/poster-" + poster + ".jpg");
        movie.setSummary(summary);
        movie.setViews(1200 + poster * 317);
        movie.setFeatured(featured);
        movie.setCreatedAt(LocalDateTime.now().minusDays(poster * 2L));
        return movie;
    }
}
