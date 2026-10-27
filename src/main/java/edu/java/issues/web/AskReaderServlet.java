package edu.java.issues.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import edu.java.issues.model.IssueRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Страница «Чьи книги показать?» (Web, лабораторная работа № 7: передача
 * данных между запросами). Выбранный читатель сохраняется двумя способами:
 * в атрибуте сессии (его использует страница выдач) и в cookie
 * <code>lastReader</code>, которая предлагает тот же выбор при следующем
 * заходе. После сохранения браузер перенаправляется на страницу выдач.
 *
 * @author Рябов И.А., группа 5315
 * @version 2.0
 */
public class AskReaderServlet extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Срок жизни cookie в секундах: 30 суток. */
	private static final int COOKIE_AGE = 30 * 24 * 3600;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String last = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("lastReader".equals(cookie.getName())) {
					last = java.net.URLDecoder.decode(cookie.getValue(),
							StandardCharsets.UTF_8);
				}
			}
		}
		request.setAttribute("readers", IssueRepository.readers());
		request.setAttribute("lastReader", last);
		request.setAttribute("csrf",
				WebSupport.csrfToken(request.getSession()));
		request.getRequestDispatcher("/WEB-INF/views/ask-reader.jsp").forward(request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!WebSupport.csrfValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"Неверный защитный токен формы");
			return;
		}
		String reader = request.getParameter("reader");
		boolean known = reader != null
				&& (IssueRepository.ALL_READERS.equals(reader)
						|| IssueRepository.readers().contains(reader));
		if (!known) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Неизвестный читатель");
			return;
		}
		request.getSession().setAttribute(WebSupport.READER_FILTER, reader);
		Cookie cookie = new Cookie("lastReader",
				URLEncoder.encode(reader, StandardCharsets.UTF_8));
		cookie.setMaxAge(COOKIE_AGE);
		cookie.setHttpOnly(true);
		cookie.setPath(request.getContextPath().isEmpty() ? "/"
				: request.getContextPath());
		response.addCookie(cookie);
		response.sendRedirect(response.encodeRedirectURL(
				request.getContextPath() + "/issues"));
	}
}
