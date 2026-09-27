document.addEventListener("DOMContentLoaded", () => {
    const state = {
        keyword: "",
        type: "全部",
        sort: "rating"
    };
    const categories = ["全部", "剧情", "喜剧", "科幻", "动画", "爱情", "犯罪"];
    const categoryRow = document.getElementById("category-row");
    const movieGrid = document.getElementById("movie-grid");
    const movieEmpty = document.getElementById("movie-empty");
    const searchForm = document.getElementById("header-search");
    const searchInput = document.getElementById("search-input");
    const sortSelect = document.getElementById("sort-select");

    categoryRow.innerHTML = categories.map(type => `
        <button class="category-button ${type === state.type ? "active" : ""}" type="button"
                data-type="${type}">${type}</button>
    `).join("");

    categoryRow.addEventListener("click", event => {
        const button = event.target.closest("[data-type]");
        if (!button) {
            return;
        }
        state.type = button.dataset.type;
        categoryRow.querySelectorAll(".category-button").forEach(item => {
            item.classList.toggle("active", item === button);
        });
        loadMovies();
    });

    searchForm.addEventListener("submit", event => {
        event.preventDefault();
        state.keyword = searchInput.value.trim();
        loadMovies();
        document.getElementById("movie-list").scrollIntoView({behavior: "smooth"});
    });

    sortSelect.addEventListener("change", () => {
        state.sort = sortSelect.value;
        loadMovies();
    });

    loadFeatured();
    loadMovies();
    loadRankings();
    loadRecentReviews();

    async function loadFeatured() {
        try {
            const movie = await MovieHub.api("/api/movies/featured");
            document.getElementById("featured-title").textContent = movie.title;
            document.getElementById("featured-meta").textContent =
                `${MovieHub.movieMeta(movie)} · ${movie.director || "导演未知"}`;
            document.getElementById("featured-summary").textContent = movie.summary || "";
            document.getElementById("featured-rating").textContent =
                Number(movie.rating || 0).toFixed(1);
            const link = `/detail.html?id=${movie.id}`;
            document.getElementById("featured-link").href = link;
            document.getElementById("featured-poster-link").href = link;
            const poster = document.getElementById("featured-poster");
            poster.src = movie.posterUrl || "/assets/posters/poster-1.jpg";
            poster.alt = `${movie.title}海报`;
        } catch (error) {
            document.getElementById("featured-title").textContent = "暂时无法加载推荐电影";
            document.getElementById("featured-summary").textContent = error.message;
        }
    }

    async function loadMovies() {
        movieGrid.innerHTML = `<div class="empty-state"><strong>正在加载电影...</strong></div>`;
        const query = new URLSearchParams({
            keyword: state.keyword,
            type: state.type,
            sort: state.sort
        });
        try {
            const movies = await MovieHub.api(`/api/movies?${query}`);
            movieGrid.innerHTML = movies.map(renderMovieCard).join("");
            movieEmpty.hidden = movies.length > 0;
        } catch (error) {
            movieGrid.innerHTML = "";
            movieEmpty.hidden = false;
            movieEmpty.querySelector("strong").textContent = "电影加载失败";
            movieEmpty.querySelector("span").textContent = error.message;
        }
    }

    function renderMovieCard(movie) {
        return `
            <article class="movie-card">
                <a class="movie-card-poster" href="/detail.html?id=${movie.id}">
                    <img src="${MovieHub.escapeHtml(movie.posterUrl || "/assets/posters/poster-1.jpg")}"
                         alt="${MovieHub.escapeHtml(movie.title)}海报" loading="lazy">
                    <span class="movie-score">${Number(movie.rating || 0).toFixed(1)}</span>
                </a>
                <div class="movie-card-body">
                    <h3><a href="/detail.html?id=${movie.id}">${MovieHub.escapeHtml(movie.title)}</a></h3>
                    <p class="movie-meta">${MovieHub.escapeHtml(MovieHub.movieMeta(movie))}</p>
                    <div class="movie-rating-line">
                        <span>${MovieHub.scoreStars(movie.rating)}</span>
                        <small>${movie.ratingCount || 0} 人评价</small>
                    </div>
                </div>
            </article>
        `;
    }

    async function loadRankings() {
        const list = document.getElementById("ranking-list");
        try {
            const movies = await MovieHub.api("/api/movies/rankings?limit=7");
            list.innerHTML = movies.map((movie, index) => `
                <li class="ranking-item">
                    <span class="rank-number">${index + 1}</span>
                    <a href="/detail.html?id=${movie.id}">
                        <img src="${MovieHub.escapeHtml(movie.posterUrl)}" alt="" loading="lazy">
                    </a>
                    <a class="ranking-title" href="/detail.html?id=${movie.id}">
                        ${MovieHub.escapeHtml(movie.title)}
                        <span>${MovieHub.escapeHtml(movie.type || "")}</span>
                    </a>
                    <span class="ranking-score">${Number(movie.rating || 0).toFixed(1)}</span>
                </li>
            `).join("");
        } catch (error) {
            list.innerHTML = `<li class="form-message error">${MovieHub.escapeHtml(error.message)}</li>`;
        }
    }

    async function loadRecentReviews() {
        const container = document.getElementById("recent-reviews");
        try {
            const reviews = await MovieHub.api("/api/movies/recent-reviews");
            const movies = await MovieHub.api("/api/movies?sort=latest");
            const movieMap = new Map(movies.map(movie => [movie.id, movie]));
            container.innerHTML = reviews.slice(0, 6).map(review => {
                const movie = movieMap.get(review.movieId) || {title: "电影"};
                return `
                    <article class="review-mini">
                        <div class="review-mini-head">
                            <img src="${MovieHub.escapeHtml(review.avatarUrl || "/assets/avatars/avatar-6.jpg")}" alt="">
                            <div>
                                <strong>${MovieHub.escapeHtml(review.nickname)}</strong>
                                <small>评价《${MovieHub.escapeHtml(movie.title)}》 · ${MovieHub.formatDate(review.createdAt)}</small>
                            </div>
                        </div>
                        <p>${MovieHub.escapeHtml(review.content)}</p>
                    </article>
                `;
            }).join("");
        } catch (error) {
            container.innerHTML = `<div class="empty-state"><strong>短评加载失败</strong><span>${MovieHub.escapeHtml(error.message)}</span></div>`;
        }
    }
});
