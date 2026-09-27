document.addEventListener("DOMContentLoaded", () => {
    const tabs = document.querySelectorAll(".auth-tab");
    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(item => item.classList.toggle("active", item === tab));
            loginForm.hidden = tab.dataset.tab !== "login";
            registerForm.hidden = tab.dataset.tab !== "register";
        });
    });

    loginForm.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("login-message");
        message.className = "form-message";
        message.textContent = "正在登录...";
        try {
            const session = await MovieHub.api("/api/auth/login", {
                method: "POST",
                body: {
                    username: document.getElementById("login-username").value,
                    password: document.getElementById("login-password").value
                }
            });
            MovieHub.setSession(session);
            location.href = nextUrl();
        } catch (error) {
            message.className = "form-message error";
            message.textContent = error.message;
        }
    });

    registerForm.addEventListener("submit", async event => {
        event.preventDefault();
        const message = document.getElementById("register-message");
        message.className = "form-message";
        message.textContent = "正在创建账号...";
        try {
            const session = await MovieHub.api("/api/auth/register", {
                method: "POST",
                body: {
                    username: document.getElementById("register-username").value,
                    nickname: document.getElementById("register-nickname").value,
                    password: document.getElementById("register-password").value
                }
            });
            MovieHub.setSession(session);
            message.textContent = "注册成功，正在返回...";
            location.href = nextUrl();
        } catch (error) {
            message.className = "form-message error";
            message.textContent = error.message;
        }
    });

    function nextUrl() {
        const next = new URLSearchParams(location.search).get("next");
        return next && next.startsWith("/") ? next : "/";
    }
});
