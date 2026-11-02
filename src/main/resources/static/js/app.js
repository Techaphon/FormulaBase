(function () {
  const themeKey = "fb_theme";
  const theme = localStorage.getItem(themeKey) || "light";
  document.documentElement.classList.toggle("fb-dark", theme === "dark");

  window.fbSetLanguage = function (lang) {
    if (!["th", "en"].includes(lang)) return;
    const url = new URL(location.href);
    url.searchParams.set("lang", lang);
    location.assign(url);
  };
  window.fbToggleTheme = function () {
    const dark = !document.documentElement.classList.contains("fb-dark");
    localStorage.setItem(themeKey, dark ? "dark" : "light");
    document.documentElement.classList.toggle("fb-dark", dark);
    fbUpdateThemeButton();
  };
  window.fbUpdateThemeButton = function () {
    const button = document.getElementById("fb-theme-toggle");
    if (button) button.textContent = document.documentElement.classList.contains("fb-dark") ? "☀" : "☾";
  };

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-lang]").forEach((button) => {
      const selected = button.dataset.lang === document.documentElement.lang;
      button.classList.toggle("active", selected);
      button.setAttribute("aria-pressed", String(selected));
    });
    document.querySelectorAll(".fb-sidebar nav a").forEach((link) => {
      const selected = location.pathname === link.pathname || location.pathname.startsWith(link.pathname + "/");
      link.classList.toggle("active", selected);
      if (selected) link.setAttribute("aria-current", "page");
    });
    fbUpdateThemeButton();
  });

  document.addEventListener("DOMContentLoaded", function () {
    const token = (document.cookie.split("; ").find((value) => value.startsWith("XSRF-TOKEN=")) || "").split("=")[1];
    if (!token) return;
    document.querySelectorAll('form[method="post"],form[method="POST"]').forEach((form) => {
      if (!form.querySelector('input[name="_csrf"]')) {
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = "_csrf";
        input.value = decodeURIComponent(token);
        form.append(input);
      }
    });
  });

  document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("complete-batch");
    if (!form) return;
    form.addEventListener("submit", function (event) {
      const actual = Number(form.elements.actualProduced.value);
      const rejected = Number(form.elements.rejected.value);
      if (rejected <= actual && (actual - rejected < Number(form.dataset.target) || rejected > Number(form.dataset.allowance))) {
        let confirmation = form.querySelector('[name="confirmException"]');
        if (!confirmation) {
          const label = document.createElement("label");
          label.className = "error";
          confirmation = document.createElement("input");
          confirmation.type = "checkbox";
          confirmation.name = "confirmException";
          label.append(confirmation, document.createTextNode(form.dataset.warning));
          form.insertBefore(label, form.querySelector("button"));
          form.querySelectorAll('input[type="number"]').forEach((input) => input.addEventListener("input", () => { confirmation.checked = false; }));
        }
        if (!confirmation.checked) {
          event.preventDefault();
          confirmation.focus();
        }
      }
    });
  });

  document.addEventListener("DOMContentLoaded", function () {
    const timers = [...document.querySelectorAll(".step-timer[data-started-at][data-target-seconds]")];
    if (!timers.length) return;
    const asClock = (seconds) => {
      const value = Math.max(0, Math.floor(seconds));
      return String(Math.floor(value / 60)).padStart(2, "0") + ":" + String(value % 60).padStart(2, "0");
    };
    const refresh = () => timers.forEach((timer) => {
      const started = Date.parse(timer.dataset.startedAt);
      const target = Number(timer.dataset.targetSeconds);
      if (Number.isNaN(started) || !Number.isFinite(target)) return;
      const elapsed = (Date.now() - started) / 1000;
      const overdue = elapsed > target;
      timer.querySelector("strong").textContent = overdue ? "+" + asClock(elapsed - target) : asClock(elapsed) + " / " + asClock(target);
      timer.classList.toggle("error", overdue);
    });
    refresh();
    window.setInterval(refresh, 1000);
  });
})();
