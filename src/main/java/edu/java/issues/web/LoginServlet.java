package edu.java.issues.web;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Страница входа (Web, лабораторная работа № 6). GET показывает форму,
 * POST проверяет имя и пароль. После успешного входа создаётся новая
 * сессия, чтобы нельзя было подсунуть пользователю чужой идентификатор.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class LoginServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Журнал входов (лабораторная работа № 10 курса ООП). */
	private static final Logger log = LogManager.getLogger(LoginServlet.class);

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("next", safeNext(request.getParameter("next")));
		request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String name = request.getParameter("username");
		String next = safeNext(request.getParameter("next"));
		Optional<Role> role = UserStore.getInstance().authenticate(name,
				request.getParameter("password"));
		if (role.isEmpty()) {
			log.warn("Неудачная попытка входа: {}", name);
			request.setAttribute("next", next);
			request.setAttribute("loginError", Boolean.TRUE);
			request.setAttribute("username", name);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request,
					response);
			return;
		}
		HttpSession old = request.getSession(false);
		Object lang = old == null ? null : old.getAttribute(WebSupport.LANG);
		if (old != null) {
			old.invalidate();
		}
		HttpSession session = request.getSession(true);
		session.setAttribute(WebSupport.USER, name);
		session.setAttribute(WebSupport.ROLE, role.get());
		if (lang != null) {
			session.setAttribute(WebSupport.LANG, lang);
		}
		log.info("Вход выполнен: {} ({})", name, role.get());
		response.sendRedirect(response.encodeRedirectURL(
				request.getContextPath() + next));
	}

	/**
	 * Оставляет только адрес внутри приложения, чтобы страница входа
	 * не могла перенаправить пользователя на чужой сайт.
	 *
	 * @param next адрес, к которому пользователь шёл до входа
	 * @return безопасный адрес внутри приложения
	 */
	static String safeNext(String next) {
		if (next == null || !next.startsWith("/") || next.startsWith("//")
				|| next.contains("\\") || next.contains("\r")
				|| next.contains("\n")) {
			return "/issues";
		}
		return next;
	}
}
