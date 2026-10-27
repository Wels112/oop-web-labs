package edu.java.issues.web;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Выход из приложения (Web, лабораторная работа № 6): завершает сессию.
 * Выполняется только методом POST, чтобы чужая страница не могла разлогинить
 * пользователя обычной ссылкой.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class LogoutServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Журнал выходов. */
	private static final Logger log = LogManager.getLogger(LogoutServlet.class);

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			log.info("Выход: {}", session.getAttribute(WebSupport.USER));
			session.invalidate();
		}
		response.sendRedirect(request.getContextPath() + "/");
	}
}
