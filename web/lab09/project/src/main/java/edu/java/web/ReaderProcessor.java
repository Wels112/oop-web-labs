package edu.java.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Обработчик формы выбора читателя (лабораторная работа № 7: передача данных
 * между запросами). Выбранный читатель сохраняется в сессии (его читает
 * страница <code>issues.jsp</code>) и в cookie <code>book.reader</code>,
 * после чего браузер перенаправляется на страницу выдач методом
 * <code>sendRedirect</code>. Если читатель не задан или не найден,
 * возвращается ошибка 400.
 *
 * @author Рябов И.А., группа 5315
 * @version 9.0
 */
public class ReaderProcessor extends HttpServlet {

	/** Номер версии класса, используемый при сериализации. */
	private static final long serialVersionUID = 1L;

	/** Срок жизни cookie в секундах. */
	private static final int COOKIE_AGE = 100;

	/**
	 * Обрабатывает отправку формы: параметр <code>reader</code>.
	 *
	 * @param request  запрос браузера
	 * @param response ответ сервера
	 * @throws ServletException при ошибке сервлета
	 * @throws IOException      при ошибке записи ответа
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		// Получение параметра из строки запроса
		String parameter = request.getParameter("reader");
		if (parameter == null || parameter.isBlank()) {
			// Сообщение об ошибке, если сервлет вызван без параметра
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Не задано имя читателя");
			return;
		}
		// Проверка наличия читателя
		if (!IssueStore.readers().contains(parameter)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Читатель с именем " + parameter + " не найден");
			return;
		}
		// Сохранение читателя в сессии
		request.getSession().setAttribute("reader", parameter);
		// Сохранение читателя в Cookie
		Cookie c = new Cookie("book.reader",
				URLEncoder.encode(parameter, StandardCharsets.UTF_8));
		// Установка времени жизни Cookie в секундах
		c.setMaxAge(COOKIE_AGE);
		c.setHttpOnly(true);
		response.addCookie(c);
		// Перенаправление на страницу выдач
		response.sendRedirect(response.encodeRedirectURL(
				request.getContextPath() + "/issues.jsp"));
	}
}
