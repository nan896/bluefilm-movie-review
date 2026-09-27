document.addEventListener("DOMContentLoaded", () => {
    const movieId = Number(new URLSearchParams(location.search).get("id") || 1);
    const detail = document.getElementById("movie-detail");
    const reviewForm = document.getElementById("review-form");
    const reviewContent = document.getElementById("review-content");
    const nicknameInput = document.getElementById("review-nickname");
    const reviewStatus = document.getElementById("review-status");
    const ratingLabel = document.getElementById("rating-label");
    const starButtons = document.querySelectorAll(".star-button");
    let selectedRating = 0;

    if (!MovieHub.getUser()) {
        nicknameInput.hidden = false;
    } else {
        nicknameInput.hidden = true;
    }

    starButtons.forEach(button => {
        button.addEventListener("click", () => {
            selectedRating = Number(button.dataset.score);
            starButtons.forEach(item => {
                item.classList.toggle("active", Number(item.dataset.score) <= selectedRating);
            });
            ratingLabel.textContent = `${selectedRating} 星`;
        });
    });

    reviewForm.addEventListener("submit", async event => {
        event.preventDefault();
        reviewStatus.className = "form-status";
        if (!selectedRating) {
            reviewStatus.className = "form-status error";
            reviewStatus.textContent = "请先选择星级";
            return;
        }
        reviewStatus.textContent = "正在发布...";
        try {
            await MovieHub.api(`/api/movies/${movieId}/reviews`, {
                method: "POST",
                body: {
                    nickname: nicknameInput.value,
                    rating: selectedRating,
                    content: reviewContent.value
                }
            });
            reviewContent.value = "";
            selectedRating = 0;
            starButtons.forEach(item => item.classList.remove("active"));
            ratingLabel.textContent = "请选择星级";
            reviewStatus.textContent = "评论发布成功";
            await loadMovie();
            await loadReviews();
        } catch (error) {
            reviewStatus.className = "form-status error";
            reviewStatus.textContent = error.message;
        }
    });

    loadMovie();
    loadReviews();

    async function loadMovie() {
        try {
            const movie = await MovieHub.api(`/api/movies/${movieId}`);
            document.title = `${movie.title} · 蓝影`;
            detail.innerHTML = `
                <div class="detail-layout">
                    <img class="detail-poster" src="${MovieHub.escapeHtml(movie.posterUrl)}"
                         alt="${MovieHub.escapeHtml(movie.title)}海报">
                    <div class="detail-copy">
                        <h1>${MovieHub.escapeHtml(movie.title)}</h1>
                        <p class="detail-original">${MovieHub.escapeHtml(movie.originalTitle || "")}</p>
                        <div class="rating-summary">
                            <strong>${Number(movie.rating || 0).toFixed(1)}</strong>
                            <span class="stars">${MovieHub.scoreStars(movie.rating)}</span>
                            <small>${movie.ratingCount || 0} 人评价 · ${movie.views || 0} 次浏览</small>
                        </div>
                        <dl class="movie-facts">
                            <dt>导演</dt><dd>${MovieHub.escapeHtml(movie.director || "暂无")}</dd>
                            <dt>主演</dt><dd>${MovieHub.escapeHtml(movie.actors || "暂无")}</dd>
                            <dt>类型</dt><dd>${MovieHub.escapeHtml(movie.type || "暂无")}</dd>
                            <dt>地区</dt><dd>${MovieHub.escapeHtml(movie.region || "暂无")}</dd>
                            <dt>上映年份</dt><dd>${movie.releaseYear || "暂无"}</dd>
                            <dt>片长</dt><dd>${movie.duration ? movie.duration + " 分钟" : "暂无"}</dd>
                        </dl>
                        <p class="movie-summary">${MovieHub.escapeHtml(movie.summary || "暂无简介")}</p>
                    </div>
                </div>
            `;
        } catch (error) {
            detail.innerHTML = `<div class="empty-state"><strong>电影加载失败</strong><span>${MovieHub.escapeHtml(error.message)}</span></div>`;
        }
    }

    async function loadReviews() {
        const list = document.getElementById("review-list");
        const empty = document.getElementById("review-empty");
        try {
            const reviews = await MovieHub.api(`/api/movies/${movieId}/reviews`);
            document.getElementById("review-count-title").textContent = `短评 · ${reviews.length}`;
            list.innerHTML = reviews.map(review => `
                <article class="review-item">
                    <img class="review-avatar"
                         src="${MovieHub.escapeHtml(review.avatarUrl || "/assets/avatars/avatar-6.jpg")}" alt="">
                    <div>
                        <div class="review-head">
                            <strong>${MovieHub.escapeHtml(review.nickname)}</strong>
                            <time>${MovieHub.formatDate(review.createdAt)}</time>
                        </div>
                        <div class="review-rating">${MovieHub.scoreStars(review.rating * 2)} ${review.rating}.0</div>
                        <p class="review-content">${MovieHub.escapeHtml(review.content)}</p>
                    </div>
                </article>
            `).join("");
            empty.hidden = reviews.length > 0;
        } catch (error) {
            list.innerHTML = `<div class="empty-state"><strong>评论加载失败</strong><span>${MovieHub.escapeHtml(error.message)}</span></div>`;
        }
    }
});
