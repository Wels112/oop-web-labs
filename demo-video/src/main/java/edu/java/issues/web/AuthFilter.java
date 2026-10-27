package edu.java.issues.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Фильтр аутентификации и авторизации (Web, лабораторная работа № 6).
 * Открыты только главная страница, страница входа и статические файлы
 * оформления. Остальные адреса требуют входа; изменять данные
 * (POST-запросы к <code>/issues</code>) может только библиотекарь.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class AuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest http = (HttpServletRequest) request;
		HttpServletResponse out = (HttpServletResponse) response;
		String path = http.getRequestURI()
				.substring(http.getContextPath().length());
		if (isPublic(path)) {
			chain.doFilter(request, response);
			return;
		}
		HttpSession session = http.getSession(false);
		Role role = session == null ? null
				: (Role) session.getAttribute(WebSupport.ROLE);
		if (role == null) {
			String next = path + (http.getQueryString() == null ? ""
					: "?" + http.getQueryString());
			out.sendRedirect(out.encodeRedirectURL(http.getContextPath()
					+ "/login?next=" + URLEncoder.encode(next,
							StandardCharsets.UTF_8)));
			return;
		}
		boolean changes = "POST".equals(http.getMethod())
				&& path.startsWith("/issues");
		if (changes && role != Role.LIBRARIAN) {
			out.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Изменять данные может только библиотекарь");
			return;
		}
		chain.doFilter(request, response);
	}

	/**
	 * Проверяет, доступен ли адрес без входа.
	 *
	 * @param path путь внутри приложения
	 * @return true для главной страницы, входа и файлов оформления
	 */
	static boolean isPublic(String path) {
		return path.equals("/") || path.equals("/index.jsp")
				|| path.equals("/login") || path.equals("/error.jsp")
				|| path.startsWith("/css/") || path.startsWith("/fonts/")
				|| path.equals("/favicon.svg");
	}
}
