const MovieHub = (() => {
    const tokenKey = "bluefilm_token";
    const userKey = "bluefilm_user";

    function getToken() {
        return localStorage.getItem(tokenKey);
    }

    function getUser() {
        try {
            return JSON.parse(localStorage.getItem(userKey));
        } catch (error) {
            return null;
        }
    }

    function setSession(session) {
        if (!session) {
            return;
        }
        localStorage.setItem(tokenKey, session.token || getToken() || "");
        localStorage.setItem(userKey, JSON.stringify(session));
        renderUserArea();
    }

    function clearSession() {
        localStorage.removeItem(tokenKey);
        localStorage.removeItem(userKey);
        renderUserArea();
        window.dispatchEvent(new CustomEvent("moviehub:logout"));
    }

    async function api(path, options = {}) {
        const headers = {};
        const token = getToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const request = {...options, headers: {...headers, ...(options.headers || {})}};
        if (request.body && typeof request.body !== "string") {
            request.headers["Content-Type"] = "application/json";
            request.body = JSON.stringify(request.body);
        }

        const response = await fetch(path, request);
        let payload;
        try {
            payload = await response.json();
        } catch (error) {
            throw new Error(`服务器返回了无法解析的数据（${response.status}）`);
        }

        if (!response.ok || payload.success === false) {
            throw new Error(payload.message || `请求失败（${response.status}）`);
        }
        return payload.data;
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatDate(value) {
        if (!value) {
            return "";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return new Intl.DateTimeFormat("zh-CN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit"
        }).format(date);
    }

    function scoreStars(score) {
        const stars = Math.max(0, Math.min(5, Math.round((Number(score) || 0) / 2)));
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }

    function movieMeta(movie) {
        return [movie.releaseYear, movie.region, movie.type].filter(Boolean).join(" · ");
    }

    function loginUrl() {
        const next = encodeURIComponent(location.pathname + location.search + location.hash);
        return `/login.html?next=${next}`;
    }

    function renderUserArea() {
        const area = document.getElementById("user-area");
        if (!area) {
            return;
        }
        const user = getUser();
        if (!user) {
            area.innerHTML = `<a class="button ghost" href="${loginUrl()}">登录 / 注册</a>`;
            return;
        }
        area.innerHTML = `
            <div class="user-chip">
                <img src="${escapeHtml(user.avatarUrl || "/assets/avatars/avatar-6.jpg")}" alt="">
                <span>${escapeHtml(user.nickname || user.username)}</span>
            </div>
            <button class="text-button" type="button" id="logout-button">退出</button>
        `;
        document.getElementById("logout-button").addEventListener("click", clearSession);
    }

    document.addEventListener("DOMContentLoaded", renderUserArea);

    return {
        api,
        clearSession,
        escapeHtml,
        formatDate,
        getToken,
        getUser,
        loginUrl,
        movieMeta,
        scoreStars,
        setSession
    };
})();
