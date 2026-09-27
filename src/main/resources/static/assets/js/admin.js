document.addEventListener("DOMContentLoaded", () => {
    const loginSection = document.getElementById("admin-login");
    const dashboard = document.getElementById("admin-dashboard");
    const loginForm = document.getElementById("admin-login-form");
    const movieForm = document.getElementById("movie-form");
    const movieDbId = document.getElementById("movie-db-id");
    const editorTitle = document.getElementById("editor-title");
    const cancelEdit = document.getElementById("cancel-edit");
    const formMessage = document.getElementById("movie-form-message");

    checkCurrentAdmin();

    loginForm.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("admin-login-message");
        message.className = "form-message";
        message.textContent = "正在登录...";
        try {
            const session = await MovieHub.api("/api/auth/login", {
                method: "POST",
                body: {
                    username: document.getElementById("admin-username").value,
                    password: document.getElementById("admin-password").value
                }
            });
            if (session.role !== "ADMIN") {
                throw new Error("这个账号不是管理员账号");
            }
            MovieHub.setSession(session);
            await enterDashboard();
        } catch (error) {
            message.className = "form-message error";
            message.textContent = error.message;
        }
    });

    movieForm.addEventListener("submit", async event => {
        event.preventDefault();
        formMessage.className = "form-message";
        formMessage.textContent = "正在保存...";
        const id = movieDbId.value;
        const payload = {
            title: document.getElementById("movie-title").value,
            originalTitle: document.getElementById("movie-original-title").value,
            releaseYear: numberValue("movie-year"),
            duration: numberValue("movie-duration"),
            type: document.getElementById("movie-type").value,
            region: document.getElementById("movie-region").value,
            director: document.getElementById("movie-director").value,
            actors: document.getElementById("movie-actors").value,
            posterUrl: document.getElementById("movie-poster").value,
            summary: document.getElementById("movie-summary").value,
            rating: numberValue("movie-rating"),
            views: numberValue("movie-views"),
            featured: document.getElementById("movie-featured").checked
        };
        try {
            await MovieHub.api(id ? `/api/admin/movies/${id}` : "/api/admin/movies", {
                method: id ? "PUT" : "POST",
                body: payload
            });
            formMessage.textContent = id ? "电影已更新" : "电影已添加";
            resetForm();
            await loadDashboardData();
        } catch (error) {
            formMessage.className = "form-message error";
            formMessage.textContent = error.message;
        }
    });

    cancelEdit.addEventListener("click", resetForm);
    document.getElementById("refresh-movies").addEventListener("click", loadDashboardData);
    window.addEventListener("moviehub:logout", showLogin);

    async function checkCurrentAdmin() {
        const user = MovieHub.getUser();
        if (!user || user.role !== "ADMIN") {
            showLogin();
            return;
        }
        try {
            const current = await MovieHub.api("/api/auth/me");
            if (current.role !== "ADMIN") {
                throw new Error("无权限");
            }
            await enterDashboard();
        } catch (error) {
            MovieHub.clearSession();
            showLogin();
        }
    }

    function showLogin() {
        loginSection.hidden = false;
        dashboard.hidden = true;
    }

    async function enterDashboard() {
        loginSection.hidden = true;
        dashboard.hidden = false;
        await loadDashboardData();
    }

    async function loadDashboardData() {
        await Promise.all([loadStats(), loadMovies()]);
    }

    async function loadStats() {
        try {
            const stats = await MovieHub.api("/api/admin/stats");
            document.getElementById("stats-grid").innerHTML = `
                <div class="stat-item"><strong>${stats.movies}</strong><span>电影数量</span></div>
                <div class="stat-item"><strong>${stats.reviews}</strong><span>评论数量</span></div>
                <div class="stat-item"><strong>${stats.users}</strong><span>用户数量</span></div>
                <div class="stat-item"><strong>${Number(stats.averageRating).toFixed(1)}</strong><span>平均评分</span></div>
            `;
        } catch (error) {
            document.getElementById("stats-grid").innerHTML =
                `<div class="form-message error">${MovieHub.escapeHtml(error.message)}</div>`;
        }
    }

    async function loadMovies() {
        const tbody = document.getElementById("admin-movie-list");
        try {
            const movies = await MovieHub.api("/api/admin/movies");
            tbody.innerHTML = movies.map(movie => `
                <tr>
                    <td>
                        <div class="table-movie">
                            <img src="${MovieHub.escapeHtml(movie.posterUrl)}" alt="">
                            <div>
                                <strong>${MovieHub.escapeHtml(movie.title)}</strong>
                                <small>${MovieHub.escapeHtml(movie.type || "")}</small>
                            </div>
                        </div>
                    </td>
                    <td>${movie.releaseYear || "-"}</td>
                    <td>${Number(movie.rating || 0).toFixed(1)}</td>
                    <td>
                        <div class="row-actions">
                            <button class="icon-button" type="button" data-edit="${movie.id}" title="编辑">
                                <svg viewBox="0 0 24 24"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4z"/></svg>
                            </button>
                            <button class="icon-button danger" type="button" data-delete="${movie.id}" title="删除">
                                <svg viewBox="0 0 24 24"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 15H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
                            </button>
                        </div>
                    </td>
                </tr>
            `).join("");
        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="4" class="form-message error">${MovieHub.escapeHtml(error.message)}</td></tr>`;
        }
    }

    document.getElementById("admin-movie-list").addEventListener("click", async event => {
        const editButton = event.target.closest("[data-edit]");
        const deleteButton = event.target.closest("[data-delete]");
        if (editButton) {
            await fillForm(editButton.dataset.edit);
        }
        if (deleteButton && confirm("确定删除这部电影及其评论吗？")) {
            try {
                await MovieHub.api(`/api/admin/movies/${deleteButton.dataset.delete}`, {method: "DELETE"});
                await loadDashboardData();
            } catch (error) {
                alert(error.message);
            }
        }
    });

    async function fillForm(id) {
        try {
            const movie = await MovieHub.api(`/api/movies/${id}`);
            movieDbId.value = movie.id;
            document.getElementById("movie-title").value = movie.title || "";
            document.getElementById("movie-original-title").value = movie.originalTitle || "";
            document.getElementById("movie-year").value = movie.releaseYear || "";
            document.getElementById("movie-duration").value = movie.duration || "";
            document.getElementById("movie-type").value = movie.type || "";
            document.getElementById("movie-region").value = movie.region || "";
            document.getElementById("movie-director").value = movie.director || "";
            document.getElementById("movie-actors").value = movie.actors || "";
            document.getElementById("movie-poster").value = movie.posterUrl || "";
            document.getElementById("movie-summary").value = movie.summary || "";
            document.getElementById("movie-rating").value = movie.rating || 0;
            document.getElementById("movie-views").value = movie.views || 0;
            document.getElementById("movie-featured").checked = Boolean(movie.featured);
            editorTitle.textContent = "编辑电影";
            cancelEdit.hidden = false;
            movieForm.scrollIntoView({behavior: "smooth", block: "start"});
        } catch (error) {
            alert(error.message);
        }
    }

    function resetForm() {
        movieForm.reset();
        movieDbId.value = "";
        document.getElementById("movie-poster").value = "/assets/posters/poster-1.jpg";
        document.getElementById("movie-rating").value = "8.0";
        document.getElementById("movie-views").value = "0";
        document.getElementById("movie-featured").checked = false;
        editorTitle.textContent = "添加电影";
        cancelEdit.hidden = true;
        formMessage.textContent = "";
    }

    function numberValue(id) {
        const value = document.getElementById(id).value;
        return value === "" ? null : Number(value);
    }
});
